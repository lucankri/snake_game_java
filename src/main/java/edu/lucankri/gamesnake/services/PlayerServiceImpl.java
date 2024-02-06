package edu.lucankri.gamesnake.services;

import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class PlayerServiceImpl implements PlayerService {
    @Override
    public String generatePlayerId() {
        return UUID.randomUUID().toString();
    }

}