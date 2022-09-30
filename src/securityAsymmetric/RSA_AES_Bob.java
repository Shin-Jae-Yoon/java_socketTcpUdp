package securityAsymmetric;

import javax.crypto.*;
import java.net.*;
import java.io.*;
import java.security.*;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.spec.SecretKeySpec;

public class RSA_AES_Bob {

    // 자바8 버전 이후의 파일 스트림 함수인 readAllBytes를 사용하기 위한 함수
    public static byte[] readAllBytes(InputStream inputStream) throws IOException {
        final int bufLen = 1024;
        byte[] buf = new byte[bufLen];
        int readLen;
        IOException exception = null;
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            while ((readLen = inputStream.read(buf, 0, bufLen)) != -1)
                outputStream.write(buf, 0, readLen);
            return outputStream.toByteArray();
        } catch (IOException e) {
            exception = e;
            throw e;
        } finally {
            if (exception == null) inputStream.close();
            else try {
                inputStream.close();
            } catch (IOException e) {
                exception.addSuppressed(e);
            }
        }
    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        System.out.println("======================================");
        System.out.println("|              Bob  서버              |");
        System.out.println("======================================");

        ServerSocket bobParentSocket = null;
        Socket bobChildSocket = null;

        try {
            // Bob 서버 소켓을 생성하여 7777번 포트와 결합(bind)
            bobParentSocket = new ServerSocket(7777);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Alice 연결 요청을 기다립니다.");
        // bobParentSocket은 bobChildSocket 연결 요청 기다림
        bobChildSocket = bobParentSocket.accept();
        System.out.println(" IP주소 : " + bobChildSocket.getInetAddress() + " 포트번호 : "
                + bobChildSocket.getPort() + "에서\n           포트번호 : " + bobChildSocket.getLocalPort() + "으로 연결요청이 들어왔습니다.");

        // 소켓의 입력스트림 얻는다.
        InputStream in = bobChildSocket.getInputStream();
        DataInputStream dis = new DataInputStream(in);

        // 소켓의 출력스트림 얻는다.
        OutputStream out = bobChildSocket.getOutputStream();
        DataOutputStream dos = new DataOutputStream(out);

        // alice에 연결 성공 메세지 보냄
        dos.writeUTF("[Notice] Bob과 연결되었습니다.\n=====================================");

        // alice로부터 연결 성공 메세지 받음
        System.out.println(dis.readUTF());

        // RSA키 생성
        SecureRandom secureRandom = new SecureRandom();
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048, secureRandom);
        KeyPair keyPair = gen.genKeyPair();

        // Alice에게 공개키 전송
        byte[] publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()).getBytes();
        dos.write(publicKey);
        System.out.println("공개키 전송 완료 ...");

        // Alice으로부터 대칭키 받음
        byte[] getKey = new byte[1000];
        byte[] byteKey = Arrays.copyOf(getKey, dis.read(getKey));
        System.out.println("대칭키 수신 완료 ...");

        // 대칭키 복호화
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
        SecretKey AesEcbKey = new SecretKeySpec(Base64.getDecoder().decode(cipher.doFinal(byteKey)), "AES");
        System.out.println("대칭키 복호화 완료 ...");

        // 평문 읽기
        FileInputStream fis = new FileInputStream("src/example.txt");
        byte[] plainText = readAllBytes(fis);
        System.out.println("평문 불러오기 완료 ...");

        // 대칭키로 평문 암호화
        cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, AesEcbKey);
        byte[] cipherText = cipher.doFinal(plainText);
        System.out.println("대칭키로 평문 암호화 완료 ...");

        // Alice에게 암호문 전송
        dos.write(cipherText);
        System.out.println("Alice에게 암호문 전송 완료 ...");
    }
}
