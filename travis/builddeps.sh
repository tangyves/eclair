pushd .
# protobuf 2.6.1
cd
wget https://github.com/google/protobuf/releases/download/v2.6.1/protobuf-2.6.1.tar.bz2
tar xjvf protobuf-2.6.1.tar.bz2
cd protobuf-2.6.1
./autogen.sh && ./configure && make && sudo make install
# protobuf-c
cd
export LD_LIBRARY_PATH=/usr/local/lib/
git clone https://github.com/protobuf-c/protobuf-c.git
cd protobuf-c
./autogen.sh && ./configure && make && sudo make install
# lightning deps
sudo add-apt-repository -y ppa:chris-lea/libsodium
sudo apt-get update
sudo apt-get install -y libsodium-dev libgmp-dev libsqlite3-dev
cd
git clone https://github.com/luke-jr/libbase58.git
cd libbase58
./autogen.sh && ./configure && make && sudo make install
# lightning
cd
git clone https://github.com/ElementsProject/lightning.git
cd lightning
git checkkout fce9ee29e3c37b4291ebb050e6a687cfaa7df95a
git submodule init
git submodule update
make
# bitcoind
cd
wget https://bitcoin.org/bin/bitcoin-core-0.13.0/bitcoin-0.13.0-x86_64-linux-gnu.tar.gz
echo "bcc1e42d61f88621301bbb00512376287f9df4568255f8b98bc10547dced96c8  bitcoin-0.13.0-x86_64-linux-gnu.tar.gz" > sha256sum.asc
sha256sum -c sha256sum.asc
tar xzvf bitcoin-0.13.0-x86_64-linux-gnu.tar.gz
popd

