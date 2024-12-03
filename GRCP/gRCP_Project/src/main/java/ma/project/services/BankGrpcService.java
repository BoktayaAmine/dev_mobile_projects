package ma.project.services;

import io.grpc.stub.StreamObserver;
import ma.project.stubs.Bank;
import ma.project.stubs.BankServiceGrpc;


public class BankGrpcService extends BankServiceGrpc.BankServiceImplBase {

    @Override
    public void convert(Bank.ConvertCurrencyRequest request, StreamObserver<Bank.ConvertCurrencyResponse> responseObserver) {
        // Extract parameters from the request
        System.out.println("Command started on port 5555...");

        String currencyFrom = request.getCurrencyFrom();
        String currencyTo = request.getCurrencyTo();
        double amount = request.getAmount();

        // Simple conversion logic with a fixed rate (example: 11.4)
        double conversionRate = 11.4;
        double result = amount * conversionRate;

        System.out.println("Received request: " + request);

        // Build the response
        Bank.ConvertCurrencyResponse response = Bank.ConvertCurrencyResponse.newBuilder()
                .setCurrencyFrom(currencyFrom)
                .setCurrencyTo(currencyTo)
                .setAmount(amount)
                .setResult(result) // The conversion result
                .build();

        // Send the response to the client
        responseObserver.onNext(response);
        // Mark the end of the gRPC call
        responseObserver.onCompleted();
    }
}
