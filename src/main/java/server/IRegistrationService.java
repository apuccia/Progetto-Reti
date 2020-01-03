package server;

import java.rmi.Remote;

public interface IRegistrationService extends Remote {
    public String registra_utente(String nickname, String password);
}
