package com.example.wsapandroidapp.Adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wsapandroidapp.Classes.ComponentManager;
import com.example.wsapandroidapp.Classes.Credentials;
import com.example.wsapandroidapp.Classes.Enums;
import com.example.wsapandroidapp.DataModel.SupplierOption;
import com.example.wsapandroidapp.DataModel.UserSupplierChecklist;
import com.example.wsapandroidapp.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SupplierOptionAdapter extends RecyclerView.Adapter<SupplierOptionAdapter.ViewHolder> {

    private final List<SupplierOption> options;

    private final  LayoutInflater layoutInflater;

    private final Context context;

    public SupplierOptionAdapter(Context context, List<SupplierOption> options) {
        this.options = options;
        this.layoutInflater = LayoutInflater.from(context);

        this.context = context;
    }

    @NonNull
    @Override
    public SupplierOptionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_supplier_comparative_sheet_input_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SupplierOptionAdapter.ViewHolder holder, int position) {
        TextView tvOption = holder.tvOption,
                tvOptionError = holder.tvOptionError;
        EditText etOption = holder.etOption;

        ComponentManager componentManager = new ComponentManager(context);

        SupplierOption supplierOption = options.get(position);

        final String[] option = {supplierOption.getOption()};

        String optionString = "Option";
        switch (position) {
            case 0:
                optionString = context.getString(R.string.option_1);
                break;
            case 1:
                optionString = context.getString(R.string.option_2);
                break;
            case 2:
                optionString = context.getString(R.string.option_3);
                break;
        }

        tvOption.setText(optionString);

        etOption.setText(supplierOption.getOption());

        componentManager.setInputRightDrawable(etOption, !Credentials.isEmpty(option[0]), Enums.CLEAR_TEXT);

        String finalOptionString = optionString;
        etOption.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                option[0] = editable != null ? editable.toString() : "";

                componentManager.setInputRightDrawable(etOption, !Credentials.isEmpty(option[0]), Enums.CLEAR_TEXT);
                checkLabel(componentManager, option[0], false, finalOptionString, tvOptionError, etOption, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return options.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvOption, tvOptionError;
        EditText etOption;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvOption = itemView.findViewById(R.id.tvOption);
            tvOptionError = itemView.findViewById(R.id.tvOptionError);
            etOption = itemView.findViewById(R.id.etOption);

            setIsRecyclable(false);
        }
    }

    private void checkLabel(ComponentManager componentManager,
                            String string, boolean isRequired, String fieldName,
                            TextView targetTextView, EditText targetEditText, int position) {
        componentManager.hideInputError(targetTextView, targetEditText);

        boolean isError = false;

        if (Credentials.isEmpty(string) && isRequired) {
            componentManager.showInputError(targetTextView,
                    context.getString(R.string.required_input_error, fieldName),
                    targetEditText);
            isError = true;
        } else if (!Credentials.isEmpty(string) && !Credentials.isValidLength(string, Credentials.REQUIRED_LABEL_LENGTH, 0)) {
            componentManager.showInputError(targetTextView,
                    Credentials.getSentenceCase(context.getString(R.string.length_error, fieldName, Credentials.REQUIRED_PERSON_NAME_LENGTH)),
                    targetEditText);
            isError = true;
        }

        if (adapterListener != null) adapterListener.onTextChange(position, string, isError);
    }

    private AdapterListener adapterListener;

    public interface AdapterListener {
        void onTextChange(int position, String option, boolean isError);
    }

    public void setAdapterListener(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }
}
