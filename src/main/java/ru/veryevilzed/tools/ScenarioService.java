package ru.veryevilzed.tools;

/**
 * Created by zed on 10.08.16.
 */
public interface ScenarioService {

    /**
     * Выполнить сценарий
     * @param name имя сценария
     * @param context контекст
     * @return контекст
     */
    Object execute(String name, String method, Object context) throws RuntimeException;

}
