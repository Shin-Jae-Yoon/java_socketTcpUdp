package securitySymmetric;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.*;
import java.security.*;
import java.util.Base64;

public class des_3des_aes_Server {

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
    // 복호화할 암호문을 cipher으로 받아옴
    // CBC는 추가적으로 초기화 벡터(Initialization Vector)를 받아옴

    // 복호화 모드 ECB
    public static byte[] ecbMode(Cipher c, Key k, byte[] cipher) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        c.init(Cipher.DECRYPT_MODE, k);
        return c.doFinal(cipher);
    }

    // 복호화 모드 CBC
    public static byte[] cbcMode(Cipher c, Key k, byte[] cipher, IvParameterSpec iv) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        c.init(Cipher.DECRYPT_MODE, k, iv);
        return c.doFinal(cipher);
    }

    public static byte[] getBytes(String fileName) throws IOException {
        File file = new File(fileName);
        FileInputStream fis = new FileInputStream(file);

        return readAllBytes(fis);
    }

    public static FileOutputStream createFile(String fileName) throws IOException {
        File file = new File(fileName);
        if (file.exists())
            file.delete();
        file.createNewFile();

        return new FileOutputStream(file);
    }

    public static void main(String[] args) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        System.out.println("======================================");
        System.out.println("|        암호화 서버 입니다.       |");
        System.out.println("======================================");

        // Udp 통신 위한 소켓 생성
        DatagramSocket datagramSocket = new DatagramSocket(11111);

        byte[] receiveFile = new byte[10000];
        DatagramPacket receiveFilePacket = new DatagramPacket(receiveFile, receiveFile.length);
        datagramSocket.receive(receiveFilePacket);

        StringBuilder fileName = new StringBuilder("ReceiveFile.txt");
        createFile(fileName.toString()).write(receiveFile, 0, receiveFilePacket.getLength());
        System.out.println("클라이언트로부터 파일 수신 완료");

        while (true) {
            try {
            byte[] receiveMode = new byte[100];
            DatagramPacket modePacket = new DatagramPacket(receiveMode, receiveMode.length);
            datagramSocket.receive(modePacket);

            String result = "";
            result = new String(modePacket.getData()).trim();

            switch (result) {
                case "DES":
                    DesDecrypt(datagramSocket);
                    break;
                case "3DES":
                    TripleDesDecrypt(datagramSocket);
                    break;
                case "AES":
                    AesDecrypt(datagramSocket);
                    break;
                default:
                    System.out.println("서버를 종료합니다.");
                    datagramSocket.close();
                    break;
            }
            System.out.println();
            } catch (Exception e) {
                datagramSocket.close();
            }
        }
    }

    public static void DesDecrypt(DatagramSocket datagramSocket) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {

        // ecb 키, 암호문 패킷 생성
        byte[] ecbKeyBuffer = new byte[10000];
        DatagramPacket ecbKeyPacket = new DatagramPacket(ecbKeyBuffer, 10000);

        byte[] ecbCipherBuffer = new byte[10000];
        DatagramPacket ecbCipherPacket = new DatagramPacket(ecbCipherBuffer, 10000);

        // 클라이언트에서 ecb 키, 암호문 패킷 수신
        System.out.println("DES ECB 모드 키, 암호문 수신중 ...");
        datagramSocket.receive(ecbKeyPacket);
        datagramSocket.receive(ecbCipherPacket);

        // ecb 키, 암호문 파일 생성
        StringBuilder ecbKeyName = new StringBuilder("src/desFile/DES_ECB_key");
        ecbKeyName.append(".txt");

        StringBuilder ecbCipherName = new StringBuilder("src/desFile/DES_ECB_cipher");
        ecbCipherName.append(".txt");

        createFile(ecbKeyName.toString()).write(ecbKeyBuffer, 0, ecbKeyPacket.getLength());
        System.out.print(ecbKeyName + "에 받은 파일이 저장되었습니다.\n");

        createFile(ecbCipherName.toString()).write(ecbCipherBuffer, 0, ecbCipherPacket.getLength());
        System.out.println(ecbCipherName + "에 받은 파일이 저장되었습니다.\n");

        // cbc 키, 암호문 패킷 생성
        byte[] cbcKeyBuffer = new byte[10000];
        DatagramPacket cbcKeyPacket = new DatagramPacket(cbcKeyBuffer, 10000);

        byte[] cbcCipherBuffer = new byte[10000];
        DatagramPacket cbcCipherPacket = new DatagramPacket(cbcCipherBuffer, 10000);

        // cbc 키, 암호문 패킷 수신
        System.out.println("DES CBC 모드 키, 암호문 수신중 ...");
        datagramSocket.receive(cbcKeyPacket);
        datagramSocket.receive(cbcCipherPacket);

        StringBuilder cbcKeyName = new StringBuilder("src/desFile/DES_CBC_key");
        cbcKeyName.append(".txt");

        StringBuilder cbcCipherName = new StringBuilder("src/desFile/DES_CBC_cipher");
        cbcCipherName.append(".txt");

        // cbc 키, 암호문 파일 생성
        createFile(cbcKeyName.toString()).write(cbcKeyBuffer, 0, cbcKeyPacket.getLength());
        System.out.print(cbcKeyName + "에 받은 파일이 저장되었습니다.\n");

        createFile(cbcCipherName.toString()).write(cbcCipherBuffer, 0, cbcCipherPacket.getLength());
        System.out.println(cbcCipherName + "에 받은 파일이 저장되었습니다.\n");

        // 복호화 시작
        System.out.println("복호화 작업 시작합니다.");

        // DES ECB 복호화
        byte[] decodedKey = Base64.getDecoder().decode(getBytes("src/desFile/DES_ECB_key.txt"));
        SecretKey key = new SecretKeySpec(decodedKey, "DES");
        byte[] cipher = getBytes("src/desFile/DES_ECB_cipher.txt");
        Cipher c = Cipher.getInstance("DES/ECB/PKCS5Padding");
        byte[] decrypted = ecbMode(c, key, cipher);

        createFile("src/desFile/DES_ECB_decrypt.txt").write(decrypted);
        System.out.println("DES ECB 복호화된 파일이 저장됐습니다.");

        // DES CBC 복호화
        byte[] iv = getBytes("src/desFile/DES_CBC_key.txt");
        cipher = getBytes("src/desFile/DES_CBC_cipher.txt");
        c = Cipher.getInstance("DES/CBC/PKCS5Padding");
        decrypted = cbcMode(c, key, cipher, new IvParameterSpec(iv));

        createFile("src/desFile/DES_CBC_decrypt.txt").write(decrypted);
        System.out.println("DES CBC 복호화된 파일이 저장됐습니다.\n");
    }

    public static void TripleDesDecrypt(DatagramSocket datagramSocket) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {

        // ecb 키, 암호문 패킷 생성
        byte[] ecbKeyBuffer = new byte[10000];
        DatagramPacket ecbKeyPacket = new DatagramPacket(ecbKeyBuffer, 10000);

        byte[] ecbCipherBuffer = new byte[10000];
        DatagramPacket ecbCipherPacket = new DatagramPacket(ecbCipherBuffer, 10000);

        // 클라이언트에서 ecb 키, 암호문 패킷 수신
        System.out.println("3DES ECB 모드 키, 암호문 수신중 ...");
        datagramSocket.receive(ecbKeyPacket);
        datagramSocket.receive(ecbCipherPacket);

        // ecb 키, 암호문 파일 생성
        StringBuilder ecbKeyName = new StringBuilder("src/desTripleFile/3DES_ECB_key");
        ecbKeyName.append(".txt");

        StringBuilder ecbCipherName = new StringBuilder("src/desTripleFile/3DES_ECB_cipher");
        ecbCipherName.append(".txt");

        createFile(ecbKeyName.toString()).write(ecbKeyBuffer, 0, ecbKeyPacket.getLength());
        System.out.print(ecbKeyName + "에 받은 파일이 저장되었습니다.\n");

        createFile(ecbCipherName.toString()).write(ecbCipherBuffer, 0, ecbCipherPacket.getLength());
        System.out.println(ecbCipherName + "에 받은 파일이 저장되었습니다.\n");

        // cbc 키, 암호문 패킷 생성
        byte[] cbcKeyBuffer = new byte[10000];
        DatagramPacket cbcKeyPacket = new DatagramPacket(cbcKeyBuffer, 10000);

        byte[] cbcCipherBuffer = new byte[10000];
        DatagramPacket cbcCipherPacket = new DatagramPacket(cbcCipherBuffer, 10000);

        // cbc 키, 암호문 패킷 수신
        System.out.println("3DES CBC 모드 키, 암호문 수신중 ...");
        datagramSocket.receive(cbcKeyPacket);
        datagramSocket.receive(cbcCipherPacket);

        StringBuilder cbcKeyName = new StringBuilder("src/desTripleFile/3DES_CBC_key");
        cbcKeyName.append(".txt");

        StringBuilder cbcCipherName = new StringBuilder("src/desTripleFile/3DES_CBC_cipher");
        cbcCipherName.append(".txt");

        // cbc 키, 암호문 파일 생성
        createFile(cbcKeyName.toString()).write(cbcKeyBuffer, 0, cbcKeyPacket.getLength());
        System.out.print(cbcKeyName + "에 받은 파일이 저장되었습니다.\n");

        createFile(cbcCipherName.toString()).write(cbcCipherBuffer, 0, cbcCipherPacket.getLength());
        System.out.println(cbcCipherName + "에 받은 파일이 저장되었습니다.\n");

        // 복호화 시작
        System.out.println("복호화 작업 시작합니다.");

        // 3DES ECB 복호화
        byte[] decodedKey = Base64.getDecoder().decode(getBytes("src/desTripleFile/3DES_ECB_key.txt"));
        SecretKey key = new SecretKeySpec(decodedKey, "TripleDES");
        byte[] cipher = getBytes("src/desTripleFile/3DES_ECB_cipher.txt");
        Cipher c = Cipher.getInstance("TripleDES/ECB/PKCS5Padding");
        byte[] decrypted = ecbMode(c, key, cipher);

        createFile("src/desTripleFile/3DES_ECB_decrypt.txt").write(decrypted);
        System.out.println("3DES ECB 복호화된 파일이 저장됐습니다.");

        // 3DES CBC 복호화
        byte[] iv = getBytes("src/desTripleFile/3DES_CBC_key.txt");
        cipher = getBytes("src/desTripleFile/3DES_CBC_cipher.txt");
        c = Cipher.getInstance("TripleDES/CBC/PKCS5Padding");
        decrypted = cbcMode(c, key, cipher, new IvParameterSpec(iv));

        createFile("src/desTripleFile/3DES_CBC_decrypt.txt").write(decrypted);
        System.out.println("3DES CBC 복호화된 파일이 저장됐습니다.\n");
    }

    public static void AesDecrypt(DatagramSocket datagramSocket) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {

        // ecb 키, 암호문 패킷 생성
        byte[] ecbKeyBuffer = new byte[10000];
        DatagramPacket ecbKeyPacket = new DatagramPacket(ecbKeyBuffer, 10000);

        byte[] ecbCipherBuffer = new byte[10000];
        DatagramPacket ecbCipherPacket = new DatagramPacket(ecbCipherBuffer, 10000);

        // 클라이언트에서 ecb 키, 암호문 패킷 수신
        System.out.println("AES ECB 모드 키, 암호문 수신중 ...");
        datagramSocket.receive(ecbKeyPacket);
        datagramSocket.receive(ecbCipherPacket);

        // ecb 키, 암호문 파일 생성
        StringBuilder ecbKeyName = new StringBuilder("src/aesFile/AES_ECB_key");
        ecbKeyName.append(".txt");

        StringBuilder ecbCipherName = new StringBuilder("src/aesFile/AES_ECB_cipher");
        ecbCipherName.append(".txt");

        createFile(ecbKeyName.toString()).write(ecbKeyBuffer, 0, ecbKeyPacket.getLength());
        System.out.print(ecbKeyName + "에 받은 파일이 저장되었습니다.\n");

        createFile(ecbCipherName.toString()).write(ecbCipherBuffer, 0, ecbCipherPacket.getLength());
        System.out.println(ecbCipherName + "에 받은 파일이 저장되었습니다.\n");

        // cbc 키, 암호문 패킷 생성
        byte[] cbcKeyBuffer = new byte[10000];
        DatagramPacket cbcKeyPacket = new DatagramPacket(cbcKeyBuffer, 10000);

        byte[] cbcCipherBuffer = new byte[10000];
        DatagramPacket cbcCipherPacket = new DatagramPacket(cbcCipherBuffer, 10000);

        // cbc 키, 암호문 패킷 수신
        System.out.println("AES CBC 모드 키, 암호문 수신중 ...");
        datagramSocket.receive(cbcKeyPacket);
        datagramSocket.receive(cbcCipherPacket);

        StringBuilder cbcKeyName = new StringBuilder("src/aesFile/AES_CBC_key");
        cbcKeyName.append(".txt");

        StringBuilder cbcCipherName = new StringBuilder("src/aesFile/AES_CBC_cipher");
        cbcCipherName.append(".txt");

        // cbc 키, 암호문 파일 생성
        createFile(cbcKeyName.toString()).write(cbcKeyBuffer, 0, cbcKeyPacket.getLength());
        System.out.print(cbcKeyName + "에 받은 파일이 저장되었습니다.\n");

        createFile(cbcCipherName.toString()).write(cbcCipherBuffer, 0, cbcCipherPacket.getLength());
        System.out.println(cbcCipherName + "에 받은 파일이 저장되었습니다.\n");

        // 복호화 시작
        System.out.println("복호화 작업 시작합니다.");

        // DES ECB 복호화
        byte[] decodedKey = Base64.getDecoder().decode(getBytes("src/aesFile/AES_ECB_key.txt"));
        SecretKey key = new SecretKeySpec(decodedKey, "AES");
        byte[] cipher = getBytes("src/aesFile/AES_ECB_cipher.txt");
        Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
        byte[] decrypted = ecbMode(c, key, cipher);

        createFile("src/aesFile/AES_ECB_decrypt.txt").write(decrypted);
        System.out.println("AES ECB 복호화된 파일이 저장됐습니다.");

        // DES CBC 복호화
        byte[] iv = getBytes("src/aesFile/AES_CBC_key.txt");
        cipher = getBytes("src/aesFile/AES_CBC_cipher.txt");
        c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        decrypted = cbcMode(c, key, cipher, new IvParameterSpec(iv));

        createFile("src/aesFile/AES_CBC_decrypt.txt").write(decrypted);
        System.out.println("AES CBC 복호화된 파일이 저장됐습니다.\n");
    }
}

