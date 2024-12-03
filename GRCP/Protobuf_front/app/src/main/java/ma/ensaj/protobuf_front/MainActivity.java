package ma.ensaj.protobuf_front;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.Socket;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import ma.project.stubs.Bank;
import ma.project.stubs.BankServiceGrpc;

public class MainActivity extends AppCompatActivity {

    private EditText amountInput, sourceCurrencyInput, targetCurrencyInput;
    private TextView convertedAmountResult;
    private Button convertButton;
    private ManagedChannel channel;
    private BankServiceGrpc.BankServiceBlockingStub stub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Updated IDs to match the XML layout
        amountInput = findViewById(R.id.et_amount);
        sourceCurrencyInput = findViewById(R.id.et_currency_from);
        targetCurrencyInput = findViewById(R.id.et_currency_to);
        convertedAmountResult = findViewById(R.id.tv_result);
        convertButton = findViewById(R.id.btn_convert);

        testServerConnection();

        setupGrpcChannel();

        convertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                convertCurrency();
            }
        });
    }

    private void testServerConnection() {
        new Thread(() -> {
            try (Socket socket = new Socket("192.168.0.119", 5555)) {
                Log.d("Network Test", "Connexion réussie au serveur !");
            } catch (Exception e) {
                Log.e("Network Test", "Échec de la connexion au serveur : " + e.getMessage(), e);
                runOnUiThread(() -> Toast.makeText(this, "Impossible d'atteindre le serveur", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void setupGrpcChannel() {
        try {
            channel = ManagedChannelBuilder.forAddress("192.168.0.119", 5555)
                    .usePlaintext()
                    .keepAliveTimeout(30, TimeUnit.SECONDS)
                    .keepAliveTime(30, TimeUnit.SECONDS)
                    .build();

            stub = BankServiceGrpc.newBlockingStub(channel);

        } catch (Exception e) {
            Log.e("gRPC Setup Error", "Erreur lors de la configuration du canal gRPC : " + e.getMessage(), e);
            Toast.makeText(this, "Échec de la configuration du canal gRPC", Toast.LENGTH_SHORT).show();
        }
    }

    private void convertCurrency() {
        String amounts = amountInput.getText().toString();
        String currencyFrom = sourceCurrencyInput.getText().toString();
        String currencyTo = targetCurrencyInput.getText().toString();

        if (amounts.isEmpty() || currencyFrom.isEmpty() || currencyTo.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amounts);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Montant invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        Bank.ConvertCurrencyRequest request = Bank.ConvertCurrencyRequest.newBuilder()
                .setAmount(amount)
                .setCurrencyFrom(currencyFrom)
                .setCurrencyTo(currencyTo)
                .build();

        new Thread(() -> {
            try {
                Bank.ConvertCurrencyResponse response = stub.convert(request);

                runOnUiThread(() -> convertedAmountResult.setText(" " + response.getResult()));

            } catch (StatusRuntimeException e) {
                Log.e("gRPC Error", "Erreur lors de l'appel gRPC : " + e.getMessage(), e);
                runOnUiThread(() -> Toast.makeText(this, "Échec de la conversion : " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Une erreur s'est produite", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
        }
    }
}
