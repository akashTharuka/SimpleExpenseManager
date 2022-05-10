/*
 * Copyright 2015 Department of Computer Science and Engineering, University of Moratuwa.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *                  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package lk.ac.mrt.cse.dbs.simpleexpensemanager;

import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.control.ExpenseManager;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.control.PersistentExpenseManager;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.control.exception.ExpenseManagerException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.DBHelper;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.InvalidAccountException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest{
    private static ExpenseManager expenseManager;
    private static DBHelper dbHelper;

    @BeforeClass
    public static void initial_setup(){
        Context context = ApplicationProvider.getApplicationContext();
        dbHelper = new DBHelper(context);
        try {
            expenseManager = new PersistentExpenseManager(context);
        } catch (ExpenseManagerException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void add_account_test(){
        expenseManager.addAccount("00000A", "TEST_BANK", "test", 10000.0);
        List<String> accountNumbers = expenseManager.getAccountNumbersList();
        assertTrue(accountNumbers.contains("00000A"));
    }

    @Test
    public void transaction_test(){
        try {
            Double old_balance = dbHelper.getAccount("12345A").getBalance();

            expenseManager.updateAccountBalance("12345A", 22, 3, 2022, ExpenseType.INCOME, "1000.0");
            Double new_balance_income = dbHelper.getAccount("12345A").getBalance();
            assertTrue(new_balance_income == old_balance + 1000.0);

            expenseManager.updateAccountBalance("12345A", 22, 3, 2022, ExpenseType.EXPENSE, "500.0");
            Double new_balance_expense = dbHelper.getAccount("12345A").getBalance();
            assertTrue(new_balance_expense == new_balance_income - 500.0);

        } catch (InvalidAccountException e) {
            e.printStackTrace();
        }
    }
}