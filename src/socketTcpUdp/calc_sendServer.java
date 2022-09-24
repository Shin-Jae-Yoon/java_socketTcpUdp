package socketTcpUdp;

import java.net.*;
import java.io.*;
import java.util.Date;
import java.util.StringTokenizer;
import java.text.SimpleDateFormat;

// 수식을 입력받고 결과를 클라이언트로 전달하는 서버
public class calc_sendServer {
    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        System.out.println("======================================");
        System.out.println("|      계산기 서버가 준비되었습니다.      |");
        System.out.println("======================================");

        try {
            // 서버 소켓을 생성하여 7777번 포트와 결합(bind)시킨다.
            serverSocket = new ServerSocket(7777);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Socket clientSocket = null;
        String receiveClient = null;
        String sendClient = "";

        while (true) {
            try {
                System.out.println(getTime() + " 연결요청을 기다립니다.");
                // 서버소켓은 클라이언트의 연결요청이 올 때까지 실행을 멈추고 계속 기다린다.
                // 클라이언트의 연결요청이 오면 클라이언트 소켓과 통신할 새로운 소켓을 생성한다.
                clientSocket = serverSocket.accept();
                System.out.println(getTime() + " IP주소 : " + clientSocket.getInetAddress() + " 포트번호 : "
                        + clientSocket.getPort() + "에서\n           포트번호 : " + clientSocket.getLocalPort() + "으로 연결요청이 들어왔습니다.");
                System.out.println();

                // 소켓의 입력스트림을 얻는다.
                InputStream in = clientSocket.getInputStream();
                DataInputStream dis = new DataInputStream(in);

                //소켓의 출력스트림을 얻는다.
                OutputStream out = clientSocket.getOutputStream();
                DataOutputStream dos = new DataOutputStream(out);

                dos.writeUTF("[Notice] 서버에 연결되었습니다.\n======================================");

                // 소켓으로부터 받은 데이터를 출력한다.

                while (true) {
                    try {
                        dos.writeUTF(sendClient);
                        dos.flush();
                    } catch (Exception e) {
                    }

                    try {
                        receiveClient = dis.readUTF();
                    } catch (Exception e) {
                        System.out.println();
                        System.out.println(getTime() + " 포트번호 : " + clientSocket.getPort() + "에서 접속을 종료했습니다.");
                        System.out.println("======================================");
                        dis.close();
                        dos.close();
                        clientSocket.close();
                        break;
                    }

//                    System.out.println("받은 수식 : " + receiveClient);

                    try {
                        sendClient = calc(receiveClient);
                    } catch (Exception e) {
                        sendClient = "숫자가 너무 크거나, 연산에 오류가 있습니다.";
                    }

                    try {
                        dos.writeUTF(sendClient);
                        dos.flush();
                    } catch (Exception e) {
                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    } //main

    // 현재시간을 문자열로 반환하는 함수
    static String getTime() {
        SimpleDateFormat f = new SimpleDateFormat("[hh:mm:ss]");
        return f.format(new Date());
    }

    public static String calc(String exp) throws IOException {
        StringTokenizer st = new StringTokenizer(exp, "+-*/", true);
        if (st.countTokens() != 3) return "연산식 오류입니다. 띄어쓰기를 없애주세요.";

        String result = "";
        double op1 = Double.parseDouble(st.nextToken());
        String opcode = st.nextToken();
        double op2 = Double.parseDouble(st.nextToken());

        System.out.println("받은 수식 : " + op1 + " " + opcode + " " + op2);
        switch (opcode) {
            case "+": case "-":
                System.out.println("덧셈/뺄셈 서버로 전송합니다.");
                result = UdpClient(exp, 8888);
                break;
            case "*":
                System.out.println("곱셈/나눗셈 서버로 전송합니다.");
                result = UdpClient(exp, 9999);
                break;
            case "/":
                if (op2 == 0) {
                    result = "0으로 나눌 수 없습니다.";
                    break;
                }
                System.out.println("곱셈/나눗셈 서버로 전송합니다.");
                result = UdpClient(exp, 9999);
                break;
            default:
                result = "error";
        }
        return result;
    }

    public static String UdpClient(String data, int portNum) throws IOException, UnknownHostException {

        DatagramSocket datagramSocket = new DatagramSocket();
        InetAddress serverAddress = InetAddress.getByName("127.0.0.1");

        // 데이터가 저장될 공간으로 byte배열을 생성
        byte[] sendCalc = data.getBytes();
        byte[] receiveResult = new byte[100];

        DatagramPacket outPacket = new DatagramPacket(sendCalc, sendCalc.length, serverAddress, portNum);
        DatagramPacket inPacket = new DatagramPacket(receiveResult, receiveResult.length);

        datagramSocket.send(outPacket);     // Datagrampacket을 전송
        datagramSocket.receive(inPacket);   // Datagrampacket을 수신

        String result = "";

        result = new String(inPacket.getData()).trim();
        datagramSocket.close();

        return result;
    }

}