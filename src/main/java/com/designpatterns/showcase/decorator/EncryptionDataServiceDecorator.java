package com.designpatterns.showcase.decorator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EncryptionDataServiceDecorator extends DataServiceDecorator {
    private static final Logger logger = LoggerFactory.getLogger(EncryptionDataServiceDecorator.class);
    private final String encryptionKey;

    public EncryptionDataServiceDecorator(DataService delegate, String encryptionKey) {
        super(delegate);
        this.encryptionKey = encryptionKey;
    }

    @Override
    public String save(String data) {
        String encrypted = encrypt(data);
        logger.debug("Encrypted data before saving (original length: {}, encrypted length: {})", 
                    data.length(), encrypted.length());
        return delegate.save(encrypted);
    }

    @Override
    public Optional<String> retrieve(String id) {
        Optional<String> encryptedData = delegate.retrieve(id);
        if (encryptedData.isPresent()) {
            String decrypted = decrypt(encryptedData.get());
            logger.debug("Decrypted data after retrieval (encrypted length: {}, decrypted length: {})", 
                        encryptedData.get().length(), decrypted.length());
            return Optional.of(decrypted);
        }
        return Optional.empty();
    }

    @Override
    public List<String> findAll() {
        List<String> encryptedList = delegate.findAll();
        logger.debug("Decrypting {} entries", encryptedList.size());
        return encryptedList.stream()
                .map(this::decrypt)
                .collect(Collectors.toList());
    }

    @Override
    public boolean delete(String id) {
        return delegate.delete(id);
    }

    @Override
    public void clearCache() {
        delegate.clearCache();
    }

    String encrypt(String plainText) {
        if (plainText == null) {
            return null;
        }
        byte[] xorResult = xor(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(xorResult);
    }

    String decrypt(String encryptedText) {
        if (encryptedText == null) {
            return null;
        }
        byte[] decoded = Base64.getDecoder().decode(encryptedText);
        byte[] xorResult = xor(decoded);
        return new String(xorResult, StandardCharsets.UTF_8);
    }

    private byte[] xor(byte[] data) {
        byte[] keyBytes = encryptionKey.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ keyBytes[i % keyBytes.length]);
        }
        return result;
    }
}
