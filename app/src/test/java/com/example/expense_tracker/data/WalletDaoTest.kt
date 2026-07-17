package com.example.expense_tracker.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class WalletDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var walletDao: WalletDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        walletDao = database.walletDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetAllWallets() {
        val wallet1 = Wallet(name = "BCA", balance = 5000000L)
        val wallet2 = Wallet(name = "Mandiri", balance = 1000000L)

        walletDao.insertWallet(wallet1)
        walletDao.insertWallet(wallet2)

        val wallets = walletDao.getAllWallets()
        assertEquals(2, wallets.size)
        assertEquals("BCA", wallets[0].name)
        assertEquals("Mandiri", wallets[1].name)
    }

    @Test
    fun getWalletById() {
        val wallet = Wallet(name = "Cash", balance = 50000L)
        walletDao.insertWallet(wallet)
        
        // Since we didn't specify ID, we get it from the list
        val insertedWallet = walletDao.getAllWallets().first()
        
        val fetchedWallet = walletDao.getWalletById(insertedWallet.id)
        assertNotNull(fetchedWallet)
        assertEquals("Cash", fetchedWallet?.name)
        assertEquals(50000L, fetchedWallet?.balance)
    }

    @Test
    fun deleteWallet() {
        val wallet = Wallet(name = "Gopay", balance = 100000L)
        walletDao.insertWallet(wallet)
        
        val insertedWallet = walletDao.getAllWallets().first()
        assertEquals(1, walletDao.getAllWallets().size)

        walletDao.deleteWallet(insertedWallet)
        
        val wallets = walletDao.getAllWallets()
        assertEquals(0, wallets.size)
        
        val fetchedWallet = walletDao.getWalletById(insertedWallet.id)
        assertNull(fetchedWallet)
    }
}
