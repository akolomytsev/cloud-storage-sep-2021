package com.geekbrains;

public enum CommandType {
    AUTH_REQUEST,  //
    AUTH_RESPONSE, //
    FILE_MESSAGE, // послать файл upload
    FILE_REQUEST, // дай мне файл download
    LIST_REQUEST, // дай мне список который у тебя есть
    LIST_RESPONSE, // список, сервер обрабатывает только LIST_REQUEST но отправляет LIST_RESPONSE
    PATH_IN_REQUEST, //
    PATH_UP_REQUEST, //
    PATH_RESPONSE, // в какой директории сейчас сервер
    DELETE
}
