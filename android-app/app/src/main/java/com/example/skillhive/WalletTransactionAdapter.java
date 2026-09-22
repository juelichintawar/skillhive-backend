package com.example.skillhive;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skillhive.model.WalletTransaction;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WalletTransactionAdapter
        extends RecyclerView.Adapter<WalletTransactionAdapter.TransactionViewHolder> {

    private final List<WalletTransaction> transactions;

    public WalletTransactionAdapter(
            List<WalletTransaction> transactions) {

        this.transactions = transactions;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_wallet_transaction,
                        parent,
                        false
                );

        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull TransactionViewHolder holder,
            int position) {

        WalletTransaction transaction =
                transactions.get(position);

        // ---------------------------------------------------------
        // Description
        // ---------------------------------------------------------

        String description =
                transaction.getDescription();

        if (description == null ||
                description.trim().isEmpty()) {

            description = "Wallet Transaction";
        }

        holder.tvDescription.setText(description);

        // ---------------------------------------------------------
        // Transaction Type
        // ---------------------------------------------------------

        String type = transaction.getType();

        if (type == null ||
                type.trim().isEmpty()) {

            type = "TRANSACTION";
        }

        holder.tvType.setText(type);

        // ---------------------------------------------------------
        // Amount
        // ---------------------------------------------------------

        Double amount = transaction.getAmount();

        if (amount != null) {

            if (amount < 0) {

                // Debit
                holder.tvAmount.setText(
                        "-₹" + String.format(
                                Locale.getDefault(),
                                "%.2f",
                                Math.abs(amount)
                        )
                );

                holder.tvAmount.setTextColor(
                        ContextCompat.getColor(
                                holder.itemView.getContext(),
                                android.R.color.holo_red_dark
                        )
                );

            } else {

                // Credit
                holder.tvAmount.setText(
                        "+₹" + String.format(
                                Locale.getDefault(),
                                "%.2f",
                                amount
                        )
                );

                holder.tvAmount.setTextColor(
                        ContextCompat.getColor(
                                holder.itemView.getContext(),
                                android.R.color.holo_green_dark
                        )
                );
            }

        } else {

            holder.tvAmount.setText("₹0.00");

            holder.tvAmount.setTextColor(
                    ContextCompat.getColor(
                            holder.itemView.getContext(),
                            android.R.color.darker_gray
                    )
            );
        }

        // ---------------------------------------------------------
        // Status
        // ---------------------------------------------------------

        String status = transaction.getStatus();

        if (status == null ||
                status.trim().isEmpty()) {

            status = "UNKNOWN";
        }

        holder.tvStatus.setText(status);

        // ---------------------------------------------------------
        // Date
        // ---------------------------------------------------------

        holder.tvDate.setText(
                formatDate(transaction.getCreatedAt())
        );
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    // -------------------------------------------------------------
    // Format Date
    // -------------------------------------------------------------

    private String formatDate(String createdAt) {

        if (createdAt == null ||
                createdAt.trim().isEmpty()) {

            return "Date unavailable";
        }

        try {

            /*
             * Backend LocalDateTime normally returns:
             *
             * 2026-09-19T17:30:45
             */

            String cleanDate = createdAt;

            if (cleanDate.contains(".")) {

                cleanDate =
                        cleanDate.substring(
                                0,
                                cleanDate.indexOf(".")
                        );
            }

            SimpleDateFormat inputFormat =
                    new SimpleDateFormat(
                            "yyyy-MM-dd'T'HH:mm:ss",
                            Locale.getDefault()
                    );

            Date date =
                    inputFormat.parse(cleanDate);

            if (date == null) {
                return createdAt;
            }

            SimpleDateFormat outputFormat =
                    new SimpleDateFormat(
                            "dd MMM yyyy, hh:mm a",
                            Locale.getDefault()
                    );

            return outputFormat.format(date);

        } catch (Exception e) {

            return createdAt;
        }
    }

    // -------------------------------------------------------------
    // ViewHolder
    // -------------------------------------------------------------

    static class TransactionViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvDescription;
        TextView tvType;
        TextView tvAmount;
        TextView tvStatus;
        TextView tvDate;

        public TransactionViewHolder(
                @NonNull View itemView) {

            super(itemView);

            tvDescription =
                    itemView.findViewById(
                            R.id.tvTransactionDescription
                    );

            tvType =
                    itemView.findViewById(
                            R.id.tvTransactionType
                    );

            tvAmount =
                    itemView.findViewById(
                            R.id.tvTransactionAmount
                    );

            tvStatus =
                    itemView.findViewById(
                            R.id.tvTransactionStatus
                    );

            tvDate =
                    itemView.findViewById(
                            R.id.tvTransactionDate
                    );
        }
    }
}