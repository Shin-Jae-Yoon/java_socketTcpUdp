package securityAsymmetric;

import java.io.*;
import java.net.*;
import javax.crypto.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Arrays;
import javax.crypto.spec.SecretKeySpec;


public class RSA_AES_Alice {
    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
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

            // Bob으로부터 공개키 받음
            byte[] getKey = new byte[1000];
            byte[] byteKey = Base64.getDecoder().decode(Arrays.copyOf(getKey, dis.read(getKey)));
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(byteKey));
            System.out.println("공개키 수신 완료 ...");

            // 대칭키(AES/ECB모드) 생성
            // AES는 128, 192, 256 사용 가능, 여기에서 AES-256비트,32바이트 키 길이 사용
            SecretKeySpec symmetricKey = new SecretKeySpec(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32}, "AES");
            byteKey = Base64.getEncoder().encodeToString(symmetricKey.getEncoded()).getBytes();

            // 생성한 대칭키를 공개키로 암호화
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byteKey = cipher.doFinal(byteKey);
            System.out.println("대칭키 암호화 완료 ...");

            // Bob에게 암호화한 대칭키 전송
            dos.write(byteKey);
            System.out.println("대칭키 전송 완료 ...");

            // Bob으로부터 암호문 수신
            byte[] getText = new byte[1000];
            byte[] cipherText = Arrays.copyOf(getText, dis.read(getText));
            System.out.println("암호문 수신 완료 ...");

            // 암호문 복호화
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, symmetricKey);
            byte[] plainText = cipher.doFinal(cipherText);

            // 복호화한 평문 파일 생성
            FileOutputStream fos = new FileOutputStream("src/securityAsymmetric/RSA_AES.txt");
            fos.write(plainText);
            System.out.println("복호화한 파일 생성 완료 ...");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
