package com.webdorphin.bot.homeworkchecker.services;

import com.webdorphin.bot.homeworkchecker.dto.remote.CodeXExecutionResponse;

public interface RemoteExecutionService {

    CodeXExecutionResponse execute();

}
