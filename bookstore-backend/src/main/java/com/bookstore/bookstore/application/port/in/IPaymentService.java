package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.HandleSepayIpnCommand;

public interface IPaymentService {

    void handleSepayIpn(HandleSepayIpnCommand command);
}
