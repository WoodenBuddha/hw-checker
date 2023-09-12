package com.webdorphin.bot.homeworkchecker.services;

import com.webdorphin.bot.homeworkchecker.dto.ProgramExecutionDto;
import com.webdorphin.bot.homeworkchecker.dto.remote.ProgramRemoteExecutionResponse;

public interface RemoteExecutionService {

    ProgramRemoteExecutionResponse execute();

}
