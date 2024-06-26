package mytown.crash;

import java.sql.Connection;
import java.sql.SQLWarning;

import cpw.mods.fml.common.ICrashCallable;
import mytown.MyTown;
import mytown.new_datasource.MyTownDatasource;
import mytown.new_datasource.MyTownUniverse;

// TODO Add more info about the datasource?

/**
 * Adds datasource info to the crash report
 */
public class DatasourceCrashCallable implements ICrashCallable {

    @Override
    public String call() throws Exception {
        MyTownDatasource datasource = MyTown.instance.datasource;
        if (datasource == null) {
            return "Datasource is not initialized yet";
        }
        String str = "";

        str += String.format(
            "Class: %s\n",
            datasource.getClass()
                .getName());
        str += String.format(
            "Stats (Towns: %s, Residents: %s, Nations: %s, Blocks: %s, Ranks: %s, Plots: %s)\n",
            MyTownUniverse.instance.towns.size(),
            MyTownUniverse.instance.residents.size(),
            0 /* MyTownUniverse.instance.getNationsMap().size() */,
            MyTownUniverse.instance.blocks.size(),
            MyTownUniverse.instance.ranks.size(),
            MyTownUniverse.instance.plots.size());
        Connection conn = datasource.getBridge()
            .getConnection();

        str += String.format("AutoCommit: %s%n", conn.getAutoCommit());
        str += String.format("----- SQL Warnings -----%n");
        str += String.format("%s8 | %s9 | %s%n", "SQLState", "ErrorCode", "Message");
        SQLWarning sqlWarning = conn.getWarnings();
        do {
            str += String.format(
                "%s8 | %s9 | %s%n",
                sqlWarning.getSQLState(),
                sqlWarning.getErrorCode(),
                sqlWarning.getMessage());
        } while (sqlWarning.getNextWarning() != null);
        return str;
    }

    @Override
    public String getLabel() {
        return "MyTown|Datasource";
    }
}
