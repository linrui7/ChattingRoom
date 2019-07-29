package com.bitee7.multclient;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class MultThreadClient {
    public static void main(String[] args) {
        //创建服务端socket。连接到127.0.0.1和端口65535上
        try {
            Socket client = new Socket("localhost", 65535);
            //从服务端读取消息
            Thread readFromServer = new Thread(new ReadFromServerThread(client));
            //写消息到服务端
            Thread writeToServer = new Thread(new WriteToServerThread(client));
            readFromServer.start();
            writeToServer.start();
            System.out.println("您已经连接到服务器，请按以下方式操作");
            System.out.println("1.登录：eg：login:name");
            System.out.println("2.私聊：eg：privateChat:name：content");
            System.out.println("3.群聊：eg：allChat:content");
            System.out.println("4.退出：eg：exit:name");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

//接收从服务端发送来的信息
class ReadFromServerThread implements Runnable {
    //创建私有的socket client,返回一个对象
    private final Socket client;
    public ReadFromServerThread(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        //服务端的输出流，由客户端的输入流接收
        try {
            Scanner in = new Scanner(client.getInputStream());
            //输出服务端发来的消息
            while (true) {

                //输出服务端发送来的消息
                if (in.hasNext()) {
                    System.out.println("服务端" + in.next());
                }
                //客户端如果关闭，显示退出消息
                if (client.isClosed()) {
                    System.out.println("客户端已经关闭");
                    break;
                }
            }
            //关闭流
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

//将消息发送给服务器。客户端的输出流
class WriteToServerThread implements Runnable {
    private Socket client;
    public WriteToServerThread(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            //获取输入信息
            Scanner scanner = new Scanner(System.in);
            //获取客户端输出流
            PrintStream out = new PrintStream(client.getOutputStream());

            while (true) {
                System.out.println("请输入信息>");
                String toServer;
                if (scanner.hasNextLine()) {
                    toServer = scanner.nextLine();
                    out.println(toServer);
                    //关闭客户端
                    if (toServer.equals("byebye")) {
                        System.out.println("关闭客户端");
                        scanner.close();
                        out.close();
                        client.close();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}