package com.geekbrains;
// служит для того что бы клиент попросил список файлов на сервере
public class ListRequest extends Command{
    @Override
    public CommandType getType() {return CommandType.LIST_REQUEST; }
}
