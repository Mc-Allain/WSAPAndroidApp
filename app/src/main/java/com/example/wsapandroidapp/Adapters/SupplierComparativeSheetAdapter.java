package com.example.wsapandroidapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wsapandroidapp.Classes.ComponentManager;
import com.example.wsapandroidapp.Classes.Credentials;
import com.example.wsapandroidapp.Classes.Enums;
import com.example.wsapandroidapp.DataModel.SupplierOption;
import com.example.wsapandroidapp.DataModel.UserSupplierComparativeSheetCategory;
import com.example.wsapandroidapp.DataModel.UserSupplierComparativeSheetItem;
import com.example.wsapandroidapp.DialogClasses.ChangeSupplierOptionOrderDialog;
import com.example.wsapandroidapp.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class SupplierComparativeSheetAdapter extends RecyclerView.Adapter {

    private final List list;

    private final  LayoutInflater layoutInflater;

    private final Context context;

    private int selectedPosition = -1;

    private ImageView imgStatus;
    private TextView tvStatus;

    public SupplierComparativeSheetAdapter(Context context, List list) {
        this.list = list;
        this.layoutInflater = LayoutInflater.from(context);

        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (getItemViewType(viewType) == 0) {
            View view = layoutInflater.inflate(R.layout.custom_supplier_checklist_category_layout, parent, false);
            return new CategoryHolder(view);
        } else {
            View view = layoutInflater.inflate(R.layout.custom_supplier_comparative_sheet_layout, parent, false);
            return new ItemHolder(view);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == 0) {
            CategoryHolder categoryHolder = (CategoryHolder) holder;
            TextView tvCategory = categoryHolder.tvCategory;

            UserSupplierComparativeSheetCategory supplierChecklistCategory =
                    (UserSupplierComparativeSheetCategory) list.get(position);

            tvCategory.setText(supplierChecklistCategory.getCategory());
        } else {
            ItemHolder itemHolder = (ItemHolder) holder;
            CardView cardView1 = itemHolder.cardView1;
            ConstraintLayout backgroundLayout1 = itemHolder.backgroundLayout1,
                    constraintLayout = itemHolder.constraintLayout;
            TextView tvItem = itemHolder.tvItem;
            ImageView imgArrow = itemHolder.imgArrow,
                    imgStatus = itemHolder.imgStatus,
                    imgFirstOption = itemHolder.imgFirstOption,
                    imgSecondOption = itemHolder.imgSecondOption,
                    imgThirdOption = itemHolder.imgThirdOption;
            EditText etFirstOption = itemHolder.etFirstOption,
                    etSecondOption = itemHolder.etSecondOption,
                    etThirdOption = itemHolder.etThirdOption,
                    etRemarks = itemHolder.etRemarks;
            TextView tvFirstOptionError = itemHolder.tvFirstOptionError,
                    tvSecondOptionError = itemHolder.tvSecondOptionError,
                    tvThirdOptionError = itemHolder.tvThirdOptionError,
                    tvRemarksError = itemHolder.tvRemarksError,
                    tvStatus = itemHolder.tvStatus;
            Button btnSave = itemHolder.btnSave,
                    btnReset = itemHolder.btnReset;

            ChangeSupplierOptionOrderDialog supplierOptionOrderDialog =
                    new ChangeSupplierOptionOrderDialog(context);

            List<TextView> errorTextViewList =
                    Arrays.asList(tvFirstOptionError, tvSecondOptionError, tvThirdOptionError);
            List<EditText> errorEditTextList =
                    Arrays.asList(etFirstOption, etSecondOption, etThirdOption);

            ComponentManager componentManager = new ComponentManager(context);
            componentManager.initializeErrorComponents(errorTextViewList, errorEditTextList);

            UserSupplierComparativeSheetItem userSupplierComparativeSheetItem =
                    (UserSupplierComparativeSheetItem) list.get(position);

            tvItem.setText(userSupplierComparativeSheetItem.getItem());

            backgroundLayout1.setBackgroundColor(context.getColor(R.color.white));
            tvItem.setTextColor(context.getColor(R.color.primary));
            imgArrow.setImageResource(R.drawable.ic_baseline_arrow_drop_down_24);
            imgArrow.setImageTintList(ColorStateList.valueOf(context.getColor(R.color.primary)));
            constraintLayout.setVisibility(View.GONE);

            if (position == selectedPosition) {
                backgroundLayout1.setBackgroundColor(context.getColor(R.color.primary));
                tvItem.setTextColor(context.getColor(R.color.white));
                imgArrow.setImageResource(R.drawable.ic_baseline_arrow_drop_up_24);
                imgArrow.setImageTintList(ColorStateList.valueOf(context.getColor(R.color.white)));
                constraintLayout.setVisibility(View.VISIBLE);
            }

            List<SupplierOption> options = new ArrayList<>(userSupplierComparativeSheetItem.getOptions().values());

            options.sort(Comparator.comparingInt(SupplierOption::getOrder));

            SupplierOption option1 = options.get(0);
            SupplierOption option2 = options.get(1);
            SupplierOption option3 = options.get(2);

            final String[] firstOption = {option1.getOption()};
            final String[] secondOption = {option2.getOption()};
            final String[] thirdOption = {option3.getOption()};
            final String[] remarks = {userSupplierComparativeSheetItem.getRemarks()};

            etFirstOption.setText(firstOption[0]);
            etSecondOption.setText(secondOption[0]);
            etThirdOption.setText(thirdOption[0]);
            etRemarks.setText(remarks[0]);

            componentManager.setInputRightDrawable(etFirstOption, !Credentials.isEmpty(firstOption[0]), Enums.CLEAR_TEXT);
            componentManager.setInputRightDrawable(etSecondOption, !Credentials.isEmpty(secondOption[0]), Enums.CLEAR_TEXT);
            componentManager.setInputRightDrawable(etThirdOption, !Credentials.isEmpty(thirdOption[0]), Enums.CLEAR_TEXT);
            componentManager.setInputRightDrawable(etRemarks, !Credentials.isEmpty(remarks[0]), Enums.CLEAR_TEXT);

            etFirstOption.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    firstOption[0] = editable != null ? editable.toString() : "";

                    componentManager.setInputRightDrawable(etFirstOption, !Credentials.isEmpty(firstOption[0]), Enums.CLEAR_TEXT);
                    checkLabel(componentManager, firstOption[0], false,
                            context.getString(R.string.option_1), tvFirstOptionError, etFirstOption);

                    if (!option1.getOption().equals(firstOption[0])) {
                        tvStatus.setText(context.getString(R.string.unsaved_changes));
                        imgStatus.setImageResource(R.drawable.ic_baseline_error_24);
                    }
                }
            });

            etSecondOption.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    secondOption[0] = editable != null ? editable.toString() : "";

                    componentManager.setInputRightDrawable(etSecondOption, !Credentials.isEmpty(secondOption[0]), Enums.CLEAR_TEXT);
                    checkLabel(componentManager, secondOption[0], false,
                            context.getString(R.string.option_2), tvSecondOptionError, etSecondOption);

                    if (!option2.getOption().equals(secondOption[0])) {
                        tvStatus.setText(context.getString(R.string.unsaved_changes));
                        imgStatus.setImageResource(R.drawable.ic_baseline_error_24);
                    }
                }
            });

            etThirdOption.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    thirdOption[0] = editable != null ? editable.toString() : "";

                    componentManager.setInputRightDrawable(etThirdOption, !Credentials.isEmpty(thirdOption[0]), Enums.CLEAR_TEXT);
                    checkLabel(componentManager, thirdOption[0], false,
                            context.getString(R.string.option_3), tvThirdOptionError, etThirdOption);

                    if (!option3.getOption().equals(thirdOption[0])) {
                        tvStatus.setText(context.getString(R.string.unsaved_changes));
                        imgStatus.setImageResource(R.drawable.ic_baseline_error_24);
                    }
                }
            });

            etRemarks.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    remarks[0] = editable != null ? editable.toString() : "";

                    componentManager.setInputRightDrawable(etRemarks, !Credentials.isEmpty(remarks[0]), Enums.CLEAR_TEXT);
                    checkLabel(componentManager, remarks[0], false,
                            context.getString(R.string.remarks), tvRemarksError, etRemarks);

                    if (!option3.getOption().equals(remarks[0])) {
                        tvStatus.setText(context.getString(R.string.unsaved_changes));
                        imgStatus.setImageResource(R.drawable.ic_baseline_error_24);
                    }
                }
            });

            imgFirstOption.setOnClickListener(view -> {
                supplierOptionOrderDialog.setSelectedOrder(1, option1);
                supplierOptionOrderDialog.showDialog();
            });

            imgSecondOption.setOnClickListener(view -> {
                supplierOptionOrderDialog.setSelectedOrder(2, option2);
                supplierOptionOrderDialog.showDialog();
            });

            imgThirdOption.setOnClickListener(view -> {
                supplierOptionOrderDialog.setSelectedOrder(3, option3);
                supplierOptionOrderDialog.showDialog();
            });

            supplierOptionOrderDialog.setDialogListener(new ChangeSupplierOptionOrderDialog.DialogListener() {
                @Override
                public void onSelect(int order, SupplierOption option) {
                    option.setOrder(order);

                    switch (order) {
                        case 1:
                            option1.setOrder(supplierOptionOrderDialog.getSelectedOrder());
                            break;
                        case 2:
                            option2.setOrder(supplierOptionOrderDialog.getSelectedOrder());
                            break;
                        case 3:
                            option3.setOrder(supplierOptionOrderDialog.getSelectedOrder());
                            break;
                    }

                    options.set(0, option1);
                    options.set(1, option2);
                    options.set(2, option3);
                    options.set(supplierOptionOrderDialog.getSelectedOrder() - 1, option);

                    Map<String, SupplierOption> supplierOptionMap = new HashMap<>();

                    for (SupplierOption supplierOption : options)
                        supplierOptionMap.put(supplierOption.getId(), supplierOption);

                    userSupplierComparativeSheetItem.setOptions(supplierOptionMap);

                    adapterListener.onSubmit(userSupplierComparativeSheetItem);

                    supplierOptionOrderDialog.dismissDialog();
                }
            });

            cardView1.setOnClickListener(view -> {
                if (selectedPosition == holder.getAdapterPosition())
                    selectedPosition = -1;
                else selectedPosition = holder.getAdapterPosition();
                notifyDataSetChanged();
            });

            btnSave.setOnClickListener(view -> {
                componentManager.hideInputErrors();

                componentManager.setInputRightDrawable(etFirstOption, !Credentials.isEmpty(firstOption[0]), Enums.CLEAR_TEXT);
                checkLabel(componentManager, firstOption[0], false,
                        context.getString(R.string.option_1), tvFirstOptionError, etFirstOption);
                componentManager.setInputRightDrawable(etSecondOption, !Credentials.isEmpty(secondOption[0]), Enums.CLEAR_TEXT);
                checkLabel(componentManager, secondOption[0], false,
                        context.getString(R.string.option_2), tvSecondOptionError, etSecondOption);
                componentManager.setInputRightDrawable(etThirdOption, !Credentials.isEmpty(thirdOption[0]), Enums.CLEAR_TEXT);
                checkLabel(componentManager, thirdOption[0], false,
                        context.getString(R.string.option_3), tvThirdOptionError, etThirdOption);

                if (componentManager.isNoInputError() && adapterListener != null) {
                    option1.setOption(Credentials.fullTrim(firstOption[0]));
                    options.set(0, option1);
                    option2.setOption(Credentials.fullTrim(secondOption[0]));
                    options.set(1, option2);
                    option3.setOption(Credentials.fullTrim(thirdOption[0]));
                    options.set(2, option3);

                    Map<String, SupplierOption> supplierOptionMap = new HashMap<>();

                    for (SupplierOption supplierOption : options)
                        supplierOptionMap.put(supplierOption.getId(), supplierOption);

                    userSupplierComparativeSheetItem.setOptions(supplierOptionMap);
                    userSupplierComparativeSheetItem.setRemarks(Credentials.fullTrim(remarks[0]));

                    adapterListener.onSubmit(userSupplierComparativeSheetItem);

                    this.imgStatus = imgStatus;
                    this.tvStatus = tvStatus;
                }
            });

            btnReset.setOnClickListener(view -> {
                etFirstOption.setText(firstOption[0]);
                etSecondOption.setText(secondOption[0]);
                etThirdOption.setText(thirdOption[0]);
                etRemarks.setText(remarks[0]);

                notifyDataSetChanged();
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (list.get(position) instanceof UserSupplierComparativeSheetCategory) return 0;
        else return 1;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class CategoryHolder extends RecyclerView.ViewHolder {

        TextView tvCategory;

        public CategoryHolder(@NonNull View itemView) {
            super(itemView);

            tvCategory = itemView.findViewById(R.id.tvCategory);

            setIsRecyclable(false);
        }
    }

    public static class ItemHolder extends RecyclerView.ViewHolder {

        CardView cardView1;
        ConstraintLayout backgroundLayout1, constraintLayout;
        TextView tvItem;
        ImageView imgArrow, imgStatus, imgFirstOption, imgSecondOption, imgThirdOption;
        EditText etFirstOption, etSecondOption, etThirdOption, etRemarks;
        TextView tvFirstOptionError, tvSecondOptionError, tvThirdOptionError, tvRemarksError, tvStatus;
        Button btnSave, btnReset;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);

            cardView1 = itemView.findViewById(R.id.cardView1);
            backgroundLayout1 = itemView.findViewById(R.id.backgroundLayout1);
            constraintLayout = itemView.findViewById(R.id.constraintLayout);
            tvItem = itemView.findViewById(R.id.tvItem);
            imgArrow = itemView.findViewById(R.id.imgArrow);

            etFirstOption = itemView.findViewById(R.id.etFirstOption);
            etSecondOption = itemView.findViewById(R.id.etSecondOption);
            etThirdOption = itemView.findViewById(R.id.etThirdOption);
            etRemarks = itemView.findViewById(R.id.etRemarks);

            tvFirstOptionError = itemView.findViewById(R.id.tvFirstOptionError);
            tvSecondOptionError = itemView.findViewById(R.id.tvSecondOptionError);
            tvThirdOptionError = itemView.findViewById(R.id.tvThirdOptionError);
            tvRemarksError = itemView.findViewById(R.id.tvRemarksError);

            imgStatus = itemView.findViewById(R.id.imgStatus);
            tvStatus = itemView.findViewById(R.id.tvStatus);

            imgFirstOption = itemView.findViewById(R.id.imgFirstOption);
            imgSecondOption = itemView.findViewById(R.id.imgSecondOption);
            imgThirdOption = itemView.findViewById(R.id.imgThirdOption);

            btnSave = itemView.findViewById(R.id.btnSave);
            btnReset = itemView.findViewById(R.id.btnReset);

            setIsRecyclable(false);
        }
    }

    private void checkLabel(ComponentManager componentManager,
                            String string, boolean isRequired, String fieldName,
                            TextView targetTextView, EditText targetEditText) {
        componentManager.hideInputError(targetTextView, targetEditText);

        if (Credentials.isEmpty(string) && isRequired)
            componentManager.showInputError(targetTextView,
                    context.getString(R.string.required_input_error, fieldName),
                    targetEditText);
        else if (!Credentials.isEmpty(string) && !Credentials.isValidLength(string, Credentials.REQUIRED_LABEL_LENGTH, 0))
            componentManager.showInputError(targetTextView,
                    context.getString(R.string.length_error, fieldName, Credentials.REQUIRED_PERSON_NAME_LENGTH),
                    targetEditText);
    }

    public void setChangesSaved() {
        if (tvStatus != null && imgStatus != null) {
            tvStatus.setText(context.getString(R.string.saved));
            imgStatus.setImageResource(R.drawable.ic_baseline_check_circle_24);
        }
    }

    private AdapterListener adapterListener;

    public interface AdapterListener {
        void onSubmit(UserSupplierComparativeSheetItem supplierComparativeSheetItem);
    }

    public void setAdapterListener(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }
}
