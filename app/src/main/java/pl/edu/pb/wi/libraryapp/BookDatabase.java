package pl.edu.pb.wi.libraryapp;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities={Book.class}, version=1, exportSchema=false)
public abstract class BookDatabase extends RoomDatabase {
    public abstract BookDao bookDao();

    private static volatile BookDatabase INSTANCE;
    public static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() -> {
                BookDao dao = INSTANCE.bookDao();
                dao.deleteAll();

                Book book = new Book("Notatka number one", "Luki");
                dao.insert(book);
                book = new Book("Notatka number two", "Luki");
                dao.insert(book);

            });
        }
    };

    static BookDatabase getDatabase(final Context context){
        if(INSTANCE==null){
            synchronized (BookDatabase.class){
                if(INSTANCE==null){
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            BookDatabase.class, "book.db")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}
