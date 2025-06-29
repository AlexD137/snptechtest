package ru.jmdevelop.snptechtest.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Data;



@Entity
@Data
public class FormByUser {

    @Id
    @Column(name = "telegram_id")
    private Long telegramId;


    private String name;

    @Email
    private String email;

    @Enumerated(EnumType.STRING)
    private UserState state;


    private Integer grade;

    public FormByUser(Long telegramId) {
        this.telegramId = telegramId;
        this.state = UserState.NEW;
    }

    protected FormByUser() {}

}
