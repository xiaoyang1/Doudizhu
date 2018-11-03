package startup;

import client.ClientProcessor;
import client.MessageSendExecutor;
import constant.MessageType;
import message.Message;
import message.UserActionMessage;
import serialize.RpcSerializeProtocol;

import java.util.Scanner;

public class ClientStart {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.println("请输入姓名登录： ");
        String name = in.next();

        // 启动一个客户端
        ClientProcessor client = new ClientProcessor(name);
        MessageSendExecutor sendExecutor = new MessageSendExecutor(client);
        sendExecutor.setClientLoader("127.0.0.1:18888", RpcSerializeProtocol.KRYOSERIALIZE);
        System.out.println("请输入您的操作：");

        while (true){
            String inputLine = in.nextLine().trim();
            // parse line

            if(inputLine.startsWith("-login")){
                client.login();
            }
            if(inputLine.startsWith("-logout")){
                client.logout();
            }

            if(inputLine.startsWith("-chat")){
                String content = inputLine.substring(6);
                client.chat(content);
            }

            if(inputLine.startsWith("-gr")){ // getRoomInfo
                client.requestForRoomInfo();
            }
            if(inputLine.startsWith("-ready")){
                client.ready();
            }

            if(inputLine.startsWith("-cancel")){
                client.cancelReady();
            }

            // 进入房间  -enter roomId
            if(inputLine.startsWith("-enter")){
                int roomId = Integer.parseInt(inputLine.split(" ")[1]);
                client.comeInRoom(roomId);
            }

            if(inputLine.startsWith("-leave")){
                client.leaveRoom();
            }

            if(inputLine.startsWith("-r")){
                char reply = inputLine.toLowerCase().charAt(3);
                client.dealWithCallDizhu(reply);
            }

            if(inputLine.startsWith("-s")) { // 展示牌
                client.showPoker();
            }

            if(inputLine.startsWith("-o")) { // 出牌， 格式为 -o  牌的下标, 用英文逗号隔开
                String cardIndex = inputLine.substring(3);
                client.outCard(cardIndex);
            }

            if(inputLine.startsWith("-p")){  // Pass, 不出牌
                client.pass();
            }

            if(inputLine.startsWith("-ls")) {  // 展示出上一个出牌的信息
                client.showLastOutCard();
            }

            if(inputLine.startsWith("-get")){  // 获得手牌
                client.getAndShowCards();
            }
        }
    }
}
