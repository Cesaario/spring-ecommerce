package app.cesario.springecommerce.controller;

import app.cesario.springecommerce.dto.ParametrosChatDTO;

import app.cesario.springecommerce.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

    @Autowired
    ChatService chatService;

    @PostMapping
    public ResponseEntity<String> conversar(@RequestBody ParametrosChatDTO parametrosChat) {
        return ResponseEntity.ok(chatService.conversar(parametrosChat.getMensagem(), parametrosChat.getIdentificador()));
    }

}
