package ru.veryevilzed.tools;

/**
 * Created by zed on 10.08.16.
 */
public interface ScenarioService {

    /**
     * Выполнить сценарий
     * @param scenario имя сценария
     * @param incoming входные параметры
     * @param context контекст
     * @return контекст
     */
    Object execute(String scenario, Object incoming, Object context) throws Exception;

    /**
     * Выполнить сценарий по умолчанию
     * @param incoming входные параметры
     * @param context контекст
     * @return контекст
     */
    Object execute(Object incoming, Object context) throws Exception;

}
