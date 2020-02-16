package server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRegistrationService extends Remote {
    public String registra_utente(String nickname, String password) throws RemoteException;
}
