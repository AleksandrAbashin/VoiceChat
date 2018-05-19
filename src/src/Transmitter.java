package src;

import javax.sound.sampled.*;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

public class Transmitter {

    MicrophoneReader mr;
    Sender sndr;
    volatile Integer numBytesRead;
    public boolean hasData = false;

    Queue<byte[]> voice = new LinkedList<>();

    public void terminate() throws Exception{
        mr.interrupt();
        sndr.interrupt();
    }

    public void main(Connection connection) {
        try {
            mr = new MicrophoneReader();
            mr.start();
                sndr = new Sender(connection);
                sndr.start();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            }
        }


    class Sender extends Thread {
        Socket s;
        Connection connection;


        public Sender( Connection connection) {
            this.connection = connection;
            this.s = connection.getSocket();
        }

        public void run() {
            try {

                while (!isInterrupted()) {
                    synchronized (voice) {

                       while (voice.size() < 1) {

                           voice.notify();
                           voice.wait();
                       }

                        connection.send(new Message(MessageType.VOICE, voice.remove()));
                        hasData = false;
                    }

                }
            } catch (Exception e) {}

        }
    }

    class MicrophoneReader extends Thread {

        AudioFormat format = new AudioFormat(16000.0f, 16, 2, true, false);
        int CHUNK_SIZE = 1024 * 100;
        TargetDataLine microphone;

        public MicrophoneReader() {

        }


        public void run() {
            try {
                microphone = AudioSystem.getTargetDataLine(format);

                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                microphone = (TargetDataLine) AudioSystem.getLine(info);
                microphone.open(format);

                microphone.start();

                while (!isInterrupted()) {
                    synchronized (voice) {
                        byte[] data;
                        data= new byte[CHUNK_SIZE];
                        numBytesRead = microphone.read(data, 0, CHUNK_SIZE);
                        if(numBytesRead > 0){

                            voice.add(data);
                            voice.notifyAll();

                            try{
                            voice.wait();}
                            catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }
        }
    }
}
