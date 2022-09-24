package socketTcpUdp;

import java.net.*;
import java.io.*;
import java.util.Date;
import java.util.StringTokenizer;
import java.text.SimpleDateFormat;

public class calc_multipleServer {
    public static void main(String[] args) throws IOException {
        System.out.println("======================================");
        System.out.println("|   곱셈/나눗셈 서버가 준비되었습니다.     |");
        System.out.println("======================================");

        DatagramSocket multipleSocket = new DatagramSocket(9999);
        DatagramPacket inPacket, outPacket;


        while (true) {
            byte[] inData = new byte[100];
            byte[] outData;
            // 데이터를 수신하기 위한 패킷 생성
            inPacket = new DatagramPacket(inData, inData.length);

            // 패킷을 통해 데이터를 수신
            multipleSocket.receive(inPacket);

            // 수신한 패킷으로부터 client의 IP주소와 Port를 얻음
            InetAddress address = inPacket.getAddress();
            int port = inPacket.getPort();

            // 수신한 패킷으로부터 입력된 연산을 가져와서 문자열로 변환
            String exp = new String(inPacket.getData()).trim();

            // 연산을 완료하고 다시 byte 배열로 변환
            outData = calc(exp).getBytes();
            System.out.println(getTime() + " 계산 완료");

            // 패킷을 생성해서 sendServer로 결과 전송
            outPacket = new DatagramPacket(outData, outData.length, address, port);
            multipleSocket.send(outPacket);
            System.out.println(getTime() + " 중앙서버로 전송 완료");
            System.out.println();
        }
    }

    static String getTime() {
        SimpleDateFormat f = new SimpleDateFormat("[hh:mm:ss]");
        return f.format(new Date());
    }

    public static String calc(String exp) {
        StringTokenizer st = new StringTokenizer(exp, "*/", true);
        if (st.countTokens() != 3) return "연산식 오류입니다.";

        String result = "";
        double op1 = Double.parseDouble(st.nextToken());
        String opcode = st.nextToken();
        double op2 = Double.parseDouble(st.nextToken());

        System.out.println(getTime() + " 도착한 수식 : " + op1 + " " + opcode + " " + op2);

        switch (opcode) {
            case "*":
                result = Double.toString(op1 * op2);
                break;
            case "/":
                result = Double.toString(op1 / op2);
                break;
            default:
                result = "error";
        }
        return result;
    }
}
