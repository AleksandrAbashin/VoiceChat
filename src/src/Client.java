package src;

import sample.Controller;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;

public class Client {

    Controller controller;
    protected Connection connection;
    public static boolean clientConnected = false;
    public static boolean clientSpeak = false ;

    String host;
    String port;
    String user;

    public Client(){}
    public Client(Controller c, String host, String port, String user){
        controller = c;
        this.host = host;
        this.user = user;
        this.port = port;
    }

    public void run() {

        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();

        // текущий поток ожидает, пока он не получит нотификацию из другого потока
        try {
            synchronized (this) {
                this.wait();
            }
        } catch (InterruptedException e) {
            ConsoleHelper.writeMessage("Ошибка");
            return;
        }

        //После того, как поток дождался нотификации, проверяем значение clientConnected
        if (clientConnected) {
            ConsoleHelper.writeMessage("Соединение установлено. Для выхода наберите команду 'exit'.");

      /*      Считывает сообщения с консоли пока клиент подключен. Если будет введена команда 'exit', то выйди из цикла
                while (clientConnected) {
                String message;
                SendVoiceMessage sendVoiceMessage = new SendVoiceMessage();

                  if (!(message = ConsoleHelper.readString()).equals("exit")) {

                    if(message .equals("s")){
                        ConsoleHelper.writeMessage("sendVoiceMessage() started");
                        startSendingVoiceMessage();

                    }
                    else if(message .equals("q")) {
                      //  sendVoiceMessage.interrupt();
                       // sendVoiceMessage.terminate();
                        terminate();
                       // connection.setClientSpeaking(false);
                        ConsoleHelper.writeMessage("sendVoiceMessage() stoped");
                    }
                    else{
                        sendTextMessage(message);
                    }
                } else {
                    return;

                }
            }*/
        }
        else {
            ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
        }
    }

    /** Запрашивает ввод адреса сервера и возвращает введенное значение**/
    protected String getServerAddress() {

        //ConsoleHelper.writeMessage("Введите адрес сервера: ");
        return host;//ConsoleHelper.readString();
    }

    protected int getServerPort() {

       // ConsoleHelper.writeMessage("Введите порт сервера: ");
       // return ConsoleHelper.readInt();
        return Integer.parseInt(port);
    }

    protected String getUserName() {

      //  ConsoleHelper.writeMessage("Введите имя пользователя: ");
       // return ConsoleHelper.readString();
        return user;
    }


    protected boolean shouldSentTextFromConsole() {
        return false;
    }


    /** Создает и возвращает объект класса SocketThread **/
    protected SocketThread getSocketThread() {

        return new SocketThread();
    }

    /**  создает новое голосовое сообщение, используя переданный массив байтов
     *  и отправляет его серверу через connection **/

    Transmitter transmitter = new Transmitter();

    public void startSendingVoiceMessage() {
        clientSpeak = true;
        transmitter.main(connection);}


        public void terminate(){
            try {
                transmitter.terminate();
            }catch (Exception e){
                e.printStackTrace();
            }
        }


    /**  создает новое текстовое сообщение, используя переданный текст и отправляет его серверу через connection **/
        public void sendTextMessage(String text) {

        try {
            connection.send(new Message(MessageType.TEXT, text));

        } catch (IOException e) {
            ConsoleHelper.writeMessage("Ошибка отправки");
            clientConnected = false;
        }
    }



    public class SocketThread extends Thread {


        public void run() {

            try {

                Socket socket = new Socket(getServerAddress(), getServerPort());

                Client.this.connection = new Connection(socket);

                clientHandshake();
                clientMainLoop();


            } catch (IOException e) {
                notifyConnectionStatusChanged(false);
            } catch (ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }

        }


        /** главный цикл обработки сообщений сервера **/
        protected void clientMainLoop() throws IOException, ClassNotFoundException {

            while (true) {

                // В цикле получаем сообщения, используя соединение connection
                Message message = connection.receive();

                switch (message.getType()) {

                    case TEXT:
                        processIncomingMessage(message.getData());
                        break;

                    case VOICE:
                        if(!clientSpeak){
                        processIncomingVoice(message.getArrayByte());}
                        break;

                    case USER_ADDED:
                        informAboutAddingNewUser(message.getData());
                        break;

                    case USER_REMOVED:
                        informAboutDeletingNewUser(message.getData());
                        break;

                    default:
                        throw new IOException("Unexpected MessageType");
                }
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException {

            while (true) {


                Message message = connection.receive();

                switch (message.getType()) {

                    case NAME_REQUEST: {

                        String userName = getUserName();
                        connection.send(new Message(MessageType.USER_NAME, userName));
                        break;                    }

                    case NAME_ACCEPTED: {
                        notifyConnectionStatusChanged(true);
                        return;
                    }

                    default: {
                        throw new IOException("Unexpected MessageType");
                    }
                }
            }
        }


        protected void informAboutAddingNewUser(String userName) {
            ConsoleHelper.writeMessage("участник " + userName + " присоединился к чату");
            controller.append("участник " + userName + " присоединился к чату");
        }

        protected void informAboutDeletingNewUser(String userName) {
            ConsoleHelper.writeMessage("участник " + userName + " покинул чат");
            controller.append("участник " + userName + " покинул чат");
        }


        protected void notifyConnectionStatusChanged(boolean clientConnected) {

            Client.this.clientConnected = clientConnected;

            synchronized (Client.this) {
                Client.this.notify();
            }
        }


        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
            controller.append(message);
        }

        protected void  processIncomingVoice(byte [] arrayBytes){


            AudioFormat format = new AudioFormat(16000.0f, 16, 2, true, false);

            SourceDataLine speakers;

            try {

                DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
                speakers = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
                speakers.open(format);
                speakers.start();

                 int numBytesRead;

                 numBytesRead = arrayBytes.length;
                 speakers.write(arrayBytes, 0, numBytesRead);
                 speakers.flush();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        }
    }
