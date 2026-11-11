package com.lumi2021.urna.controller.poolWorker;

import com.lumi2021.urna.platform.events.InputAction;
import com.lumi2021.urna.view.models.MesarioScreenModel;

import java.util.function.Consumer;

public interface PoolWorkerController {
    void handleInput(InputAction input);
    public void setViewStateListener(Consumer<MesarioScreenModel> listener);
}
