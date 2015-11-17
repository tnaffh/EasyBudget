/*
 *   Copyright 2015 Benoit LETONDOR
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.benoitletondor.easybudgetapp.helper;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Helper to work with currencies and display
 *
 * @author Benoit LETONDOR
 */
public class CurrencyHelper
{
    /**
     * List of main currencies ISO 4217 code
     */
    private static final String[] MAIN_CURRENCIES = {"USD", "EUR", "GBP", "IRN", "AUD", "CAD", "SGD", "CHF", "MYR", "JPY", "CNY", "NZD"};

// ----------------------------------------->

    /**
     * Return a list of available main currencies based on {@link #MAIN_CURRENCIES} codes
     *
     * @return a list of currencies
     */
    @NonNull
    public static List<Currency> getMainAvailableCurrencies()
    {
        List<Currency> mainCurrencies = new ArrayList<>(MAIN_CURRENCIES.length);

        for(String currencyCode : MAIN_CURRENCIES)
        {
            try
            {
                Currency currency = Currency.getInstance(currencyCode);
                if( currency != null )
                {
                    mainCurrencies.add(currency);
                }
            }
            catch (Exception e)
            {
                Logger.debug("Unable to find currency with code: "+currencyCode);
            }
        }

        return mainCurrencies;
    }

    /**
     * Return a list of available currencies (using compat code) minus main ones
     *
     * @return a list of other available currencies
     */
    public static List<Currency> getOtherAvailableCurrencies()
    {
        List<Currency> mainCurrencies = getMainAvailableCurrencies();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            List<Currency> currencies = new ArrayList<>(Currency.getAvailableCurrencies());

            // Exclude main currencies
            Iterator<Currency> currencyIterator = currencies.iterator();
            while (currencyIterator.hasNext())
            {
                Currency currency = currencyIterator.next();

                if( mainCurrencies.contains(currency) )
                {
                    currencyIterator.remove();
                }
            }

            return currencies;
        }
        else
        {
            Set<Currency> currencySet = new HashSet<>();

            Locale[] locales = Locale.getAvailableLocales();
            for(Locale locale : locales)
            {
                try
                {
                    Currency currency = Currency.getInstance(locale);

                    if( mainCurrencies.contains(currency) )
                    {
                        continue; // Exclude main currencies
                    }

                    currencySet.add(currency);
                }
                catch(Exception ignored)
                {
                    // Locale not found
                }
            }

            List<Currency> currencies = new ArrayList<>(currencySet);
            Collections.sort(currencies, new Comparator<Currency>()
            {
                @Override
                public int compare(Currency lhs, Currency rhs)
                {
                    return lhs.getCurrencyCode().compareTo(rhs.getCurrencyCode());
                }
            });

            return currencies;
        }
    }

    /**
     * Get the currency display name (using compat)
     *
     * @param currency
     * @return
     */
    public static String getCurrencyDisplayName(Currency currency)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            return currency.getSymbol()+ " - "+ currency.getDisplayName();
        }
        else
        {
            if( !currency.getSymbol().equals(currency.getCurrencyCode()) )
            {
                return currency.getSymbol()+ " - "+ currency.getCurrencyCode();
            }
            else
            {
                return currency.getSymbol();
            }
        }
    }

    /**
     * Helper to display an amount using the user currency
     *
     * @param context
     * @param amount
     * @return
     */
    public static String getFormattedCurrencyString(@NonNull Context context, int amount)
    {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();

        // No fraction digits
        currencyFormat.setMaximumFractionDigits(0);
        currencyFormat.setMinimumFractionDigits(0);

        currencyFormat.setCurrency(getUserCurrency(context));

        return currencyFormat.format(amount);
    }

    /**
     * Convenience method to get user currency
     *
     * @param context
     * @return
     */
    public static Currency getUserCurrency(@NonNull Context context)
    {
        return Currency.getInstance(Parameters.getInstance(context).getString(ParameterKeys.CURRENCY_ISO));
    }

    /**
     * Convenience method to set user currency
     *
     * @param context
     * @param currency
     */
    public static void setUserCurrency(@NonNull Context context, @NonNull Currency currency)
    {
        Parameters.getInstance(context).putString(ParameterKeys.CURRENCY_ISO, currency.getCurrencyCode());
    }
}
