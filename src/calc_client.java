import java.net.*;
import java.io.*;

public class calc_client {
    public static void main(String[] args) {
        System.out.println("======================================");
        System.out.println("|        계산기 클라이언트입니다.        |");
        System.out.println("======================================");

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        Socket socket = null;
        String sendServer = "", receiveServer = "";

        try {
            String serverIp = "127.0.0.1";
            // 소켓을 생성하여 연결을 요청한다.
            socket = new Socket(serverIp, 7777);

            // 소켓의 입력스트림을 얻는다.
            InputStream in = socket.getInputStream();
            DataInputStream dis = new DataInputStream(in);

            //소켓의 출력스트림을 얻는다.
            OutputStream out = socket.getOutputStream();
            DataOutputStream dos = new DataOutputStream(out);

            System.out.println("서버IP : " + serverIp + "에 연결중입니다.");

            // 소켓으로부터 받은 데이터를 출력한다. 서버 연결 성공 메시지
            System.out.println(dis.readUTF());

            // 계산기 수식 입력 시작
            System.out.println("수식을 입력하세요. 종료를 원하면 exit");

            while (true) {
                receiveServer = dis.readUTF();

                System.out.print(">> ");
                sendServer = br.readLine();

                if (sendServer.equals("exit")) {
                    dis.close();
                    dos.close();
                    socket.close();
                    System.out.println("연결이 종료되었습니다.");
                    break;
                }

                try {
                    dos.writeUTF(sendServer);
                } catch (Exception e) {}

                try {
                    receiveServer = dis.readUTF();
                } catch (Exception e) {}

                System.out.println("결과 : " + receiveServer);
            }

        } catch (IOException ie) {
            ie.printStackTrace();
        }
    } // main
}
