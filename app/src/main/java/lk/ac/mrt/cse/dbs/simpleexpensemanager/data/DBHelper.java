package lk.ac.mrt.cse.dbs.simpleexpensemanager.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.InvalidAccountException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Transaction;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(@Nullable Context context) {
        super(context, "190623V", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createAccountStatement = "CREATE TABLE account_table(accountNo TEXT PRIMARY KEY, bankName TEXT, accountHolderName TEXT, balance REAL)";
        String createTransactionStatement = "CREATE TABLE transaction_table(id INTEGER PRIMARY KEY AUTOINCREMENT, accountNo TEXT, expenseType INT, amount REAL, date TEXT)";
        String createExpenseTypeStatement = "CREATE TABLE expenseType_table(id INT PRIMARY KEY, type TEXT)";

        sqLiteDatabase.execSQL(createAccountStatement);
        sqLiteDatabase.execSQL(createTransactionStatement);
        sqLiteDatabase.execSQL(createExpenseTypeStatement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE account_table");
        sqLiteDatabase.execSQL("DROP TABLE transaction_table");
        sqLiteDatabase.execSQL("DROP TABLE expenseType_table");

        onCreate(sqLiteDatabase);
    }

    public void addExpenseType(int id, String expenseType){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("id", id);
        cv.put("type", expenseType);

        db.insert("expenseType_table", null, cv);
        db.close();
    }

    public void addAccount(Account account){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("accountNo", account.getAccountNo());
        cv.put("bankName", account.getBankName());
        cv.put("accountHolderName", account.getAccountHolderName());
        cv.put("balance", account.getBalance());

        db.insert("account_table", null, cv);
        db.close();
    }

    public Account getAccount(String accountNo) throws InvalidAccountException {
        String query = "SELECT * FROM account_table WHERE accountNo=?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{accountNo});

        Account account = null;

        if (cursor.moveToFirst()){
            account = new Account(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getDouble(3));
        }else{
            String msg = "Account " + accountNo + " is invalid.";
            throw new InvalidAccountException(msg);
        }

        cursor.close();
        db.close();

        return account;
    }

    public List<Account> getAccountsList() {
        List<Account> accounts = new ArrayList<>();

        String query = "SELECT * FROM account_table";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()){
            do{
                String accountNo = cursor.getString(0);
                String bankName = cursor.getString(1);
                String accountHolderName = cursor.getString(2);
                double balance = cursor.getDouble(3);

                Account account = new Account(accountNo, bankName, accountHolderName, balance);
                accounts.add(account);

            }while(cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return accounts;
    }

    public List<String> getAccountNumbersList() {
        List<String> accountNumbers = new ArrayList<>();

        String query = "SELECT accountNo FROM account_table";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()){
            do {
                accountNumbers.add(cursor.getString(0));
            }while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return accountNumbers;
    }

    public void removeAccount(String accountNo) throws InvalidAccountException {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM account_table WHERE accountNo=?", new String[]{accountNo});

        db.close();
    }

    public void updateBalance(String accountNo, ExpenseType expenseType, double amount) throws InvalidAccountException {
        Account account = getAccount(accountNo);
        double balance = account.getBalance();

        SQLiteDatabase db = this.getWritableDatabase();

        switch (expenseType){
            case EXPENSE:
                balance -= amount;
                break;
            case INCOME:
                balance += amount;
                break;
        }

        db.execSQL("UPDATE account_table SET balance=? WHERE accountNo=?", new String[]{String.valueOf(balance), accountNo});
        db.close();
    }

    public void logTransaction(Transaction transaction){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("accountNo", transaction.getAccountNo());
        cv.put("expenseType", transaction.getExpenseType().ordinal());
        cv.put("amount", transaction.getAmount());
        cv.put("date", transaction.getDate().toString());

        db.insert("transaction_table", null, cv);
        db.close();
    }

    public List<Transaction> getAllTransactionLogs() {
        List<Transaction> transactions = new ArrayList<>();

        // transactions list is made in the descending order to get the latest 10(limit) transactions to log
        String query = "SELECT accountNo, type, amount, date FROM transaction_table LEFT OUTER JOIN " +
                "expenseType_table on transaction_table.expenseType=expenseType_table.id ORDER BY " +
                "transaction_table.id DESC";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()){
            do{
                String accountNo = cursor.getString(0);
                String expenseType = cursor.getString(1);
                double amount = cursor.getDouble(2);
                Date date = new Date(cursor.getString(3));

                Transaction transaction = new Transaction(date, accountNo, ExpenseType.valueOf(expenseType), amount);
                transactions.add(transaction);

            }while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return transactions;
    }

    public List<Transaction> getPaginatedTransactionLogs(int limit) {
        List<Transaction> transactions = getAllTransactionLogs();

        int size = transactions.size();
        if (size <= limit) {
            return transactions;
        }
        // return the last <code>limit</code> number of transaction logs
        return transactions.subList(size - limit, size);
    }
}
