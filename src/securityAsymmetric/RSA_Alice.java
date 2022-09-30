package securityAsymmetric;

import java.io.*;
import java.net.*;
import javax.crypto.*;
import java.security.*;
import java.util.Base64;
import java.util.Arrays;


public class RSA_Alice {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        System.out.println("======================================");
        System.out.println("|           Alice 클라이언트           |");
        System.out.println("======================================");

        Socket socket = null;

        try {
            String serverIp = "127.0.0.1";
            // 소켓을 생성하여 Bob에 연결 요청
            socket = new Socket(serverIp, 7777);

            // 소켓의 입력스트림을 얻는다.
            InputStream in = socket.getInputStream();
            DataInputStream dis = new DataInputStream(in);

            // 소켓의 출력스트림을 얻는다.
            OutputStream out = socket.getOutputStream();
            DataOutputStream dos = new DataOutputStream(out);

            System.out.println("서버 IP : " + serverIp + "에 연결중입니다.");

            // Bob과 연결 성공 메세지 받음
            System.out.println(dis.readUTF());
            
            // Bob에 연결 성공 메세지 보냄
            dos.writeUTF("[Notice] Alice와 연결되었습니다.\n======================================");

            // RSA키 생성
            SecureRandom secureRandom = new SecureRandom();
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048, secureRandom);
            KeyPair keyPair = gen.genKeyPair();

            // Bob에게 공개키 전송
            byte[] publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()).getBytes();
            dos.write(publicKey);
            System.out.println("공개키 전송 완료 ...");

            // Bob으로부터 암호문 수신
            byte[] getText = new byte[1000];
            byte[] cipherText = Arrays.copyOf(getText, dis.read(getText));
            System.out.println("암호문 수신 완료 ...");

            // 개인키로 암호문 복호화
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
            byte[] decrypted = cipher.doFinal(cipherText);
            System.out.println("암호문 복호화 완료 ...");

            FileOutputStream fos = new FileOutputStream("src/securityAsymmetric/RSA.txt");
            fos.write(decrypted);
            System.out.println("파일 저장 완료 ...");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
