package securitySymmetric;

import java.io.*;
import java.net.*;
import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class des_3des_aes_Client {

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

    // Cipher 값으로 DES, 3DES, AES의 instance를 받아옴
    // DES, 3DES, AES 키 길이가 달라서 각각 다르게 받아옴
    // 암호화할 평문을 plain으로 받아옴
    // CBC는 추가적으로 초기화 벡터(Initialization Vector)를 받아옴

    // 암호화 모드 ECB
    public static byte[] ecbMode(Cipher c, Key k, byte[] plain) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        c.init(Cipher.ENCRYPT_MODE, k);
        return c.doFinal(plain);
    }

    // 암호화 모드 CBC
    public static byte[] cbcMode(Cipher c, Key k, byte[] plain, IvParameterSpec iv) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        c.init(Cipher.ENCRYPT_MODE, k, iv);
        return c.doFinal(plain);
    }

    // javax.crypto.spec.IvParameterSpec 클래스 이용 Intialize Vector 생성
    public static IvParameterSpec IvGenerator(Cipher c){
        SecureRandom rand = new SecureRandom();
        byte[] iv = new byte[c.getBlockSize()];
        rand.nextBytes(iv);
        return new IvParameterSpec(iv);
    }


    public static void main(String[] args) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {

        System.out.println("======================================");
        System.out.println("|        암호화 클라이언트 입니다.       |");
        System.out.println("======================================");

        // Udp 통신 위한 소켓 생성
        DatagramSocket datagramSocket = new DatagramSocket(10000);
        InetAddress serverAddress = InetAddress.getByName("127.0.0.1");
        System.out.println("전송할 서버 주소 : " + serverAddress.getHostAddress());

        // Udp 서버 포트
        int portNum = 11111;

        // 평문 example.txt 준비
        FileInputStream fis = new FileInputStream("src/example.txt");
        byte[] buffer = readAllBytes(fis);
        System.out.println("평문 파일이 준비되었습니다.");

        DatagramPacket packet=new DatagramPacket(buffer, buffer.length, serverAddress, portNum);
        datagramSocket.send(packet);
        System.out.println("서버로 파일 전송 완료\n");

        // 암호화 종류 입력 위한 버퍼리더
        String selectMode = "", selectResult = "";
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("DES/3DES/AES 원하는 모드를 입력하세요.");
        System.out.println("         종료를 원하면 exit         ");
        System.out.println("======================================");

        // 암호화 종류 입력 알고리즘 -> 각 암호화에 맞게 함수 이용
        while (true) {
            System.out.print("모드 : ");
            selectMode = br.readLine();
            byte[] outMode = selectMode.getBytes();
            DatagramPacket modePacket = new DatagramPacket(outMode, outMode.length, serverAddress, portNum);
            datagramSocket.send(modePacket);

            if (selectMode.equals("exit")) {
                System.out.println("연결이 종료되었습니다.");
                datagramSocket.close();
                break;
            }

            switch (selectMode) {
                case "DES":
                    DesEncrypt(serverAddress, portNum, datagramSocket, buffer);
                    break;
                case "3DES":
                    TripleDesEncrypt(serverAddress, portNum, datagramSocket, buffer);
                    break;
                case "AES":
                    AesEncrypt(serverAddress, portNum, datagramSocket, buffer);
                    break;
                default:
                    selectResult = "올바르지 않은 모드입니다. 다시 입력해주세요.";
                    System.out.println(selectResult);
                    break;
            }
        }
    }

    public static void DesEncrypt(InetAddress serverAddress, int portNum, DatagramSocket datagramSocket, byte[] buffer) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException, IOException, InvalidAlgorithmParameterException {
        // DES 암호화 ECB 모드
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");

        // DES는 64비트 ( 56비트 키 + 8비트 패리티 비트 키 ) = 8바이트 길이의 키를 이용
        DESKeySpec desKeySpec = new DESKeySpec(new byte[]{1, 2, 3, 4, 5, 6, 7, 8});
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        Key key = keyFactory.generateSecret(desKeySpec);

        byte[] keyBytes = Base64.getEncoder().encodeToString(key.getEncoded()).getBytes();
        byte[] cipherText = ecbMode(cipher, key, buffer);

        DatagramPacket desECB_Key_packet = new DatagramPacket(keyBytes, keyBytes.length, serverAddress, portNum);
        DatagramPacket desECB_Cipher_packet = new DatagramPacket(cipherText, cipherText.length, serverAddress, portNum);
        datagramSocket.send(desECB_Key_packet);
        datagramSocket.send(desECB_Cipher_packet);

        System.out.println("DES ECB 전송 완료");

        // DES 암호화 CBC 모드
        cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");

        // CBC 모드는 초기화 벡터 사용
        IvParameterSpec iv = IvGenerator(cipher);
        cipherText = cbcMode(cipher, key, buffer, iv);

        DatagramPacket desCBC_Key_packet = new DatagramPacket(iv.getIV(), iv.getIV().length, serverAddress, portNum);
        DatagramPacket desCBC_Cipher_packet = new DatagramPacket(cipherText, cipherText.length, serverAddress, portNum);

        datagramSocket.send(desCBC_Key_packet);
        datagramSocket.send(desCBC_Cipher_packet);

        System.out.println("DES CBC 전송 완료\n");
    }

    public static void TripleDesEncrypt(InetAddress serverAddress, int portNum, DatagramSocket datagramSocket, byte[] buffer) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException, IOException, InvalidAlgorithmParameterException {
        // Triple DES 암호화 ECB 모드
        Cipher cipher = Cipher.getInstance("TripleDES/ECB/PKCS5Padding");

        // Triple DES는 192비트 ( DES 3번 ) = 24바이트 길이의 키를 이용
        DESedeKeySpec tripleDesKeySpec = new DESedeKeySpec(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24});
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("TripleDES");
        Key key = keyFactory.generateSecret(tripleDesKeySpec);

        byte[] keyBytes = Base64.getEncoder().encodeToString(key.getEncoded()).getBytes();
        byte[] cipherText = ecbMode(cipher, key, buffer);

        DatagramPacket tripledesECB_Key_packet = new DatagramPacket(keyBytes, keyBytes.length, serverAddress, portNum);
        DatagramPacket tripledesECB_Cipher_packet = new DatagramPacket(cipherText, cipherText.length, serverAddress, portNum);
        datagramSocket.send(tripledesECB_Key_packet);
        datagramSocket.send(tripledesECB_Cipher_packet);

        System.out.println("Triple DES ECB 전송 완료");

        // Triple DES 암호화 CBC 모드
        cipher = Cipher.getInstance("TripleDES/CBC/PKCS5Padding");

        // CBC 모드는 초기화 벡터 사용
        IvParameterSpec iv = IvGenerator(cipher);
        cipherText = cbcMode(cipher, key, buffer, iv);

        DatagramPacket tripledesCBC_Key_packet = new DatagramPacket(iv.getIV(), iv.getIV().length, serverAddress, portNum);
        DatagramPacket tripledesCBC_Cipher_packet = new DatagramPacket(cipherText, cipherText.length, serverAddress, portNum);

        datagramSocket.send(tripledesCBC_Key_packet);
        datagramSocket.send(tripledesCBC_Cipher_packet);

        System.out.println("Triple DES CBC 전송 완료\n");
    }

    public static void AesEncrypt(InetAddress serverAddress, int portNum, DatagramSocket datagramSocket, byte[] buffer) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException, IOException, InvalidAlgorithmParameterException {
        // AES 암호화 ECB 모드
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

        // AES는 128, 192, 256 사용 가능, 여기에서 AES-256비트,32바이트 키 길이 사용
        SecretKeySpec AesKeySpec = new SecretKeySpec(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32}, "AES");

        byte[] keyBytes = Base64.getEncoder().encodeToString(AesKeySpec.getEncoded()).getBytes();
        byte[] cipherText = ecbMode(cipher, AesKeySpec, buffer);

        DatagramPacket AesECB_Key_packet = new DatagramPacket(keyBytes, keyBytes.length, serverAddress, portNum);
        DatagramPacket AesECB_Cipher_packet = new DatagramPacket(cipherText, cipherText.length, serverAddress, portNum);
        datagramSocket.send(AesECB_Key_packet);
        datagramSocket.send(AesECB_Cipher_packet);

        System.out.println("AES ECB 전송 완료");

        // AES 암호화 CBC 모드
        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        // CBC 모드는 초기화 벡터 사용
        IvParameterSpec iv = IvGenerator(cipher);
        cipherText = cbcMode(cipher, AesKeySpec, buffer, iv);

        DatagramPacket AesCBC_Key_packet = new DatagramPacket(iv.getIV(), iv.getIV().length, serverAddress, portNum);
        DatagramPacket AesCBC_Cipher_packet = new DatagramPacket(cipherText, cipherText.length, serverAddress, portNum);

        datagramSocket.send(AesCBC_Key_packet);
        datagramSocket.send(AesCBC_Cipher_packet);

        System.out.println("Triple DES CBC 전송 완료\n");
    }
}