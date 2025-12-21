package com.bs_enterprises.enterprise_backend_template.services;

import org.springframework.stereotype.Service;

@Service
public interface CryptoService {

    String encrypt(String plaintext);

    String decrypt(String packedCiphertext);

}
