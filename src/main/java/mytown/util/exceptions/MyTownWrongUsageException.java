package mytown.util.exceptions;

import net.minecraft.command.WrongUsageException;

import mytown.MyTown;

public class MyTownWrongUsageException extends WrongUsageException {

    public MyTownWrongUsageException(String key, Object... args) {
        super(
            MyTown.instance.LOCAL.getLocalization(key, args)
                .getUnformattedText());
    }
}
