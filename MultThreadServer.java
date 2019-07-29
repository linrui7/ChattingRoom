package com.bitee7.multserver;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultThreadServer {
    public static void main(String[] args) {
        try {
            //创建线程池
            ExecutorService executorService = Executors.newFixedThreadPool(20);
            //创建服务端口号
            ServerSocket serverSocket = new ServerSocket(65535);
            for (int i = 0; i < 20; i++) {
                System.out.println("等待客户端进行连接");
                //客户端连接
                Socket client = serverSocket.accept();
                System.out.println("客户端连接成功,端口号为" + client.getInetAddress());
                executorService.submit(new ExecuteClient(client));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //处理客户端的信息 实现功能
    public static class ExecuteClient implements Runnable {
        private Socket client;
        private static final Map<String, Socket> ONLINE_CLIENT_MAP = new ConcurrentHashMap<>();

        public ExecuteClient(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                //接收来自客户端的输入流
                InputStream clientInput = client.getInputStream();
                Scanner scanner = new Scanner(clientInput);
                while (true) {
                     /*
                       1.登录：eg：login:name
                       2.私聊：eg：privateChat:name：content
                       3.群聊：eg：allChat:content
                       4.退出：eg：exit:name
                      */
                    String MessageFromClient = scanner.nextLine();
                    //判断客户端输入的消息，给出相应的功能操作
                    if (MessageFromClient.startsWith("login")) {
                        System.out.println("client使用-->登录功能");
                        String userName=MessageFromClient.split(":")[1];
                        login(userName);
                        continue;
                    }
                    if (MessageFromClient.startsWith("privateChat")) {
                        System.out.println("client使用-->私聊功能");
                        String targetName=MessageFromClient.split(":")[1];
                        String message=MessageFromClient.split(":")[2];
                        privateChat(targetName,message);
                        continue;
                    }
                    if (MessageFromClient.startsWith("allChat")) {
                        System.out.println("client使用-->群聊功能");
                        String message=MessageFromClient.split(":")[1];
                        allChat(message);
                        continue;
                    }
                    if (MessageFromClient.startsWith("exit")) {
                        System.out.println("client使用-->退出功能");
                        logout();
                        continue;
                    } else {
                        System.out.println("客户端输入有误，请按以下格式输入以执行");
                        System.out.println("1.登录：eg：login:name");
                        System.out.println("2.私聊：eg：privateChat:name：content");
                        System.out.println("3.群聊：eg：allChat:content");
                        System.out.println("4.退出：eg：exit:name");
                        continue;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        //登录功能实现
        private void login(String userName) {
            ONLINE_CLIENT_MAP.put(userName,this.client);
            printOnlineClient();
            this.sendMessage(this.client,"-->"+userName+"登录成功",false);
        }

        //实现发送消息的功能
        private void sendMessage(Socket client, String message,boolean prefix) {
            OutputStream clientOutput=null;
            try {
                //获取客户端的输出流
                clientOutput=client.getOutputStream();
                //输出流
                OutputStreamWriter writer=new OutputStreamWriter(clientOutput);
                //给别人发消息时，是否给自己也发送一条
                if(prefix){
                    String currentClientName=this.getCurrentClientName();
                    writer.write(currentClientName+"说："+message+"\n");
                }else{
                    writer.write(message+"\n");
                }
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //获取当前客户的姓名
        private String getCurrentClientName() {
            //哈希表中已经保存了所有注册的用户
            for (Map.Entry<String, Socket> entry : ONLINE_CLIENT_MAP.entrySet()) {
                Socket target = entry.getValue();
                if (target.equals(this.client)) {
                    return entry.getKey();
                }
            }
            return "";
        }

        //打印当前在线的客户
        private void printOnlineClient() {
            System.out.println("当前在线用户如下");
            for(String clientName:ONLINE_CLIENT_MAP.keySet()){
                System.out.println(clientName);
            }
        }

        //私聊功能实现
        private void privateChat(String targetName, String message) {
            //从表中获取要对话的姓名
            Socket target=ONLINE_CLIENT_MAP.get(targetName);
            if(target != null){
                this.sendMessage(target,message,true);
            }else{
                this.sendMessage(target,"没有这个人",true);
            }
        }
        //群聊功能实现
        private void allChat(String message) {
            //用foreach循环获取每个在线用户，进行群发
            for(Map.Entry<String,Socket> entry:ONLINE_CLIENT_MAP.entrySet()){
                //获取哈希表中的每个用户
                Socket target=entry.getValue();
                if(target.equals(this.client)){
                    continue;
                }
                this.sendMessage(target,message,true);
            }
        }
        //退出功能实现
        private void logout() {
            //获取当前哈斯表中保存的所有用户
            for(Map.Entry<String,Socket> entry:ONLINE_CLIENT_MAP.entrySet()){
                Socket target=entry.getValue();
                //从表中移除用户
                if(target.equals(this.client)){
                    ONLINE_CLIENT_MAP.remove(entry.getKey());
                }
            }
            //最后打印当前在线用户
            printOnlineClient();
        }
    }
}
