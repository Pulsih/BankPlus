# Additions

- CMI Hook
- Various AFK options:
  - Pay AFK players the offline interest amount
  - Pay AFK players a different rate
    - Add `Afk-Interest` to your `bank.yml` for changes to take effect.
    - `AFK-Interest` is also accepted
    - Defaults to 4%
 - Fixed "Your bank is full" message displaying when paying interest to vault balance was enabled

# Building

In order for you to be able to build this version of BankPlus, you'll need to do some extra stuff.

1. Download [CMI-API](https://github.com/Zrips/CMI-API/releases/latest)
2. Create a directory in root called `libs` and drop the file in there.
3. Add the jar as a dependency for whatever IDE you're using.

   3a.  If using IntelliJ: *File > Project Structure > Modules > Dependencies > JARs or Directories*
4. Set the language level to 17
   
   4a.  If using IntelliJ: *File > Project Structure > Project > Language Level > 17*

That should be all.
