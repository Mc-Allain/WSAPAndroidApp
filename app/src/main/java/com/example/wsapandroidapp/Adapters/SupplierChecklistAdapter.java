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
import com.example.wsapandroidapp.DataModel.UserSupplierChecklist;
import com.example.wsapandroidapp.DataModel.UserSupplierChecklistCategory;
import com.example.wsapandroidapp.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class SupplierChecklistAdapter extends RecyclerView.Adapter {

    private final List list;

    private final  LayoutInflater layoutInflater;

    private final Context context;

    private int selectedPosition = -1;

    public SupplierChecklistAdapter(Context context, List list) {
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
            View view = layoutInflater.inflate(R.layout.custom_supplier_checklist_layout, parent, false);
            return new TaskHolder(view);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == 0) {
            CategoryHolder categoryHolder = (CategoryHolder) holder;
            TextView tvCategory = categoryHolder.tvCategory;

            UserSupplierChecklistCategory supplierChecklistCategory =
                    (UserSupplierChecklistCategory) list.get(position);

            tvCategory.setText(supplierChecklistCategory.getCategory());

            List<UserSupplierChecklist> userSupplierChecklist =
                    new ArrayList<>(supplierChecklistCategory.getChecklist().values());

            userSupplierChecklist.sort(Comparator.comparingInt(UserSupplierChecklist::getOrder));
        } else {
            TaskHolder taskHolder = (TaskHolder) holder;
            CardView cardView1 = taskHolder.cardView1;
            ConstraintLayout backgroundLayout1 = taskHolder.backgroundLayout1,
                    constraintLayout = taskHolder.constraintLayout;
            TextView tvTask = taskHolder.tvTask;
            ImageView imgArrow = taskHolder.imgArrow;
            EditText etCompany = taskHolder.etCompany,
                    etContactPerson = taskHolder.etContactPerson,
                    etPhoneNumber = taskHolder.etPhoneNumber,
                    etEmailAddress = taskHolder.etEmailAddress;
            TextView tvCompanyError = taskHolder.tvCompanyError,
                    tvContactPersonError = taskHolder.tvContactPersonError,
                    tvPhoneNumberError = taskHolder.tvPhoneNumberError,
                    tvEmailAddressError = taskHolder.tvEmailAddressError;
            Button btnSave = taskHolder.btnSave;

            List<TextView> errorTextViewList =
                    Arrays.asList(tvCompanyError, tvContactPersonError,
                            tvPhoneNumberError, tvEmailAddressError);
            List<EditText> errorEditTextList =
                    Arrays.asList(etCompany, etContactPerson, etPhoneNumber, etEmailAddress);

            ComponentManager componentManager = new ComponentManager(context);
            componentManager.initializeErrorComponents(errorTextViewList, errorEditTextList);

            UserSupplierChecklist supplierChecklist =
                    (UserSupplierChecklist) list.get(position);

            tvTask.setText(supplierChecklist.getTask());

            final String[] company = {supplierChecklist.getCompany()};
            final String[] contactPerson = {supplierChecklist.getContactPerson()};

            final String[] contactNumber = {""};
            if (String.valueOf(supplierChecklist.getContactNumber()).length() > 0 &&
                    supplierChecklist.getContactNumber() != 0)
                contactNumber[0] = String.valueOf(supplierChecklist.getContactNumber());

            final String[] emailAddress = {supplierChecklist.getEmailAddress()};

            etCompany.setText(company[0]);
            etContactPerson.setText(contactPerson[0]);
            etPhoneNumber.setText(contactNumber[0]);
            etEmailAddress.setText(emailAddress[0]);

            backgroundLayout1.setBackgroundColor(context.getColor(R.color.white));
            tvTask.setTextColor(context.getColor(R.color.primary));
            imgArrow.setImageResource(R.drawable.ic_baseline_arrow_drop_down_24);
            imgArrow.setImageTintList(ColorStateList.valueOf(context.getColor(R.color.primary)));
            constraintLayout.setVisibility(View.GONE);

            if (position == selectedPosition) {
                backgroundLayout1.setBackgroundColor(context.getColor(R.color.primary));
                tvTask.setTextColor(context.getColor(R.color.white));
                imgArrow.setImageResource(R.drawable.ic_baseline_arrow_drop_up_24);
                imgArrow.setImageTintList(ColorStateList.valueOf(context.getColor(R.color.white)));
                constraintLayout.setVisibility(View.VISIBLE);
            }

            etCompany.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    company[0] = editable != null ? editable.toString() : "";

                    componentManager.setInputRightDrawable(etCompany, !Credentials.isEmpty(company[0]), Enums.CLEAR_TEXT);
                    checkPersonName(componentManager, company[0], false,
                            context.getString(R.string.company), tvCompanyError, etCompany);
                }
            });

            etContactPerson.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    contactPerson[0] = editable != null ? editable.toString() : "";

                    componentManager.setInputRightDrawable(etContactPerson, !Credentials.isEmpty(contactPerson[0]), Enums.CLEAR_TEXT);
                    checkPersonName(componentManager, contactPerson[0], false,
                            context.getString(R.string.contact_person), tvContactPersonError, etContactPerson);
                }
            });

            etPhoneNumber.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    contactNumber[0] = editable != null ? editable.toString() : "";

                    componentManager.setInputRightDrawable(etPhoneNumber, !Credentials.isEmpty(contactNumber[0]), Enums.CLEAR_TEXT);
                    checkPhoneNumberError(componentManager, tvPhoneNumberError, etPhoneNumber, contactNumber[0]);
                }
            });

            etEmailAddress.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    emailAddress[0] = editable != null ? editable.toString() : "";

                    componentManager.setInputRightDrawable(etEmailAddress, !Credentials.isEmpty(emailAddress[0]), Enums.CLEAR_TEXT);
                    checkEmailAddressError(componentManager, tvEmailAddressError, etEmailAddress, emailAddress[0]);
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

                checkPersonName(componentManager, company[0], false,
                        context.getString(R.string.company), tvCompanyError, etCompany);
                checkPersonName(componentManager, contactPerson[0], false,
                        context.getString(R.string.contact_person), tvContactPersonError, etContactPerson);
                checkPhoneNumberError(componentManager, tvPhoneNumberError, etPhoneNumber, contactNumber[0]);
                checkEmailAddressError(componentManager, tvEmailAddressError, etEmailAddress, emailAddress[0]);

                if (componentManager.isNoInputError() && adapterListener != null) {
                    long parsedPhoneNumber = 0;
                    if (!Credentials.isEmpty(contactNumber[0])) parsedPhoneNumber = Long.parseLong(contactNumber[0]);

                    supplierChecklist.setCompany(company[0]);
                    supplierChecklist.setContactPerson(contactPerson[0]);
                    supplierChecklist.setContactNumber(parsedPhoneNumber);
                    supplierChecklist.setEmailAddress(emailAddress[0]);

                    adapterListener.onSubmit(supplierChecklist);
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (list.get(position) instanceof UserSupplierChecklistCategory) return 0;
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

    public static class TaskHolder extends RecyclerView.ViewHolder {

        CardView cardView1;
        ConstraintLayout backgroundLayout1, constraintLayout;
        TextView tvTask;
        ImageView imgArrow;
        EditText etCompany, etContactPerson, etPhoneNumber, etEmailAddress;
        TextView tvCompanyError, tvContactPersonError, tvPhoneNumberError, tvEmailAddressError;
        Button btnSave;

        public TaskHolder(@NonNull View itemView) {
            super(itemView);

            cardView1 = itemView.findViewById(R.id.cardView1);
            backgroundLayout1 = itemView.findViewById(R.id.backgroundLayout1);
            constraintLayout = itemView.findViewById(R.id.constraintLayout);
            tvTask = itemView.findViewById(R.id.tvTask);
            imgArrow = itemView.findViewById(R.id.imgArrow);

            etCompany = itemView.findViewById(R.id.etCompany);
            etContactPerson = itemView.findViewById(R.id.etContactPerson);
            etPhoneNumber = itemView.findViewById(R.id.etPhoneNumber);
            etEmailAddress = itemView.findViewById(R.id.etEmailAddress);

            tvCompanyError = itemView.findViewById(R.id.tvCompanyError);
            tvContactPersonError = itemView.findViewById(R.id.tvContactPersonError);
            tvPhoneNumberError = itemView.findViewById(R.id.tvPhoneNumberError);
            tvEmailAddressError = itemView.findViewById(R.id.tvEmailAddressError);

            btnSave = itemView.findViewById(R.id.btnSave);

            setIsRecyclable(false);
        }
    }

    private void checkPersonName(ComponentManager componentManager,
                                 String string, boolean isRequired, String fieldName,
                                 TextView targetTextView, EditText targetEditText) {
        componentManager.hideInputError(targetTextView, targetEditText);

        if (Credentials.isEmpty(string) && isRequired)
            componentManager.showInputError(targetTextView,
                    context.getString(R.string.required_input_error, fieldName),
                    targetEditText);
        else if (!Credentials.isEmpty(string) && !Credentials.isValidLength(string, Credentials.REQUIRED_LABEL_LENGTH, 0))
            componentManager.showInputError(targetTextView,
                    Credentials.getSentenceCase(context.getString(R.string.length_error, fieldName, Credentials.REQUIRED_PERSON_NAME_LENGTH)),
                    targetEditText);
    }

    private void checkPhoneNumberError(ComponentManager componentManager,
                                       TextView tvPhoneNumberError, EditText etPhoneNumber,
                                       String contactNumber) {
        componentManager.hideInputError(tvPhoneNumberError, etPhoneNumber);

        if (!Credentials.isEmpty(contactNumber) &&
                !Credentials.isValidPhoneNumber(contactNumber,
                        Credentials.REQUIRED_PHONE_NUMBER_LENGTH_PH_WITH_PREFIX,
                        Credentials.REQUIRED_PHONE_NUMBER_LENGTH_PH))
            componentManager.showInputError(tvPhoneNumberError,
                    context.getString(R.string.invalid_error, context.getString(R.string.phone_number)),
                    etPhoneNumber);
    }

    private void checkEmailAddressError(ComponentManager componentManager,
                                        TextView tvEmailAddressError, EditText etEmailAddress,
                                        String emailAddress) {
        componentManager.hideInputError(tvEmailAddressError, etEmailAddress);

        if (!Credentials.isEmpty(emailAddress) && !Credentials.isValidEmailAddress(emailAddress))
            componentManager.showInputError(tvEmailAddressError,
                    context.getString(R.string.invalid_error, context.getString(R.string.email_address)),
                    etEmailAddress);
    }

    private AdapterListener adapterListener;

    public interface AdapterListener {
        void onSubmit(UserSupplierChecklist supplierChecklist);
    }

    public void setAdapterListener(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }
}
