package com.geekbrains;

import java.io.IOException;

public interface Callback {
    void call(Command cmd) throws IOException, InterruptedException;
}
