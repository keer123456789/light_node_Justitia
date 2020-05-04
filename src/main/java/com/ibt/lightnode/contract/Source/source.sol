pragma solidity ^0.4.11;

contract pigtrace {
    address From;
    address To;
    string Location;
    string buyRole;
    string sellRole;
    string newPigEvent;
    string changePigEvent;
    string TransferEvent;

    struct Pig {
        //所有者地址
        address currentAddress;
        //出生时间
        uint256 birthTime;
        //品种
        uint256 breed;
        //uint256 id;
        //重量
        uint256 weight;
        //年龄
        uint256 age;
        //养殖场
        uint256 pigFactory;
    }
    //定义新pig事件
    event newPig(string newPigEvent, uint256 pigID, address currentAddress, uint256 birthTime, uint256 breed, uint256 weight, uint256 age, uint256 pigfactory);
    //定义更改pig事件
    event changePig(string changePigEvent, uint256 pigID, address currentAddress, uint256 changeTime, uint256 breed, uint256 newWeight, uint256 newAge, uint256 pigFactory);
    //定义转移pig所有权事件
    event Transfer (address From, address To, uint256 pigID, string buyRole, string sellRole, string Location, uint256 price);


    Pig[] pigs;
    //记录pig是属于哪个账户地址的
    mapping(uint256 => address) public pigToOwner;
    //记录某个账户地址里有多少pig
    mapping(address => uint) public ownerPigCount;
    //mapping (address => uint256) public ownerBalance;

    function creatPig(uint256 _breed, uint256 _weight, uint256 _age, uint256 _pigFactory) external returns (uint256){

        Pig  memory _pig = Pig({
            currentAddress : msg.sender,
            birthTime : uint256(now),
            breed : _breed,
            //id : _id,
            weight : _weight,
            age : _age,
            //status : 0,
            pigFactory : _pigFactory
            });
        uint256 newtokenId = pigs.push(_pig) - 1;
        newPigEvent = "newPig";
        emit newPig(newPigEvent, newtokenId, msg.sender, uint256(now), _breed, _weight, _age, _pigFactory);

        //将pig分配给函数调用者
        pigToOwner[newtokenId] = msg.sender;
        //msg.sender 名下的pig数量 加 1
        ownerPigCount[msg.sender]++;

    }

    function changePigInfo(uint256 _tokenId, uint256 _breed, uint256 _newWeight, uint256 _newAge, uint256 _pigFactory) external returns (uint256){
        //只有pig所有人可以更改信息
        require(pigToOwner [_tokenId] == msg.sender);

        pigs[_tokenId].birthTime = uint64(now);
        pigs[_tokenId].breed = _breed;
        // pigs[_tokenId].id = _id;
        pigs[_tokenId].weight = _newWeight;
        pigs[_tokenId].age = _newAge;
        pigs[_tokenId].pigFactory = _pigFactory;
        changePigEvent = "changePig";
        emit changePig(changePigEvent, _tokenId, msg.sender, uint256(now), _breed, _newWeight, _newAge, _pigFactory);
    }


    //设置一只猪的主人地址
    function _transfer(address _from, address _to, uint256 _tokenId, string _buyRole, string _sellRole, string _location, uint256 _value) external returns (uint256){
        //pig的位置
        uint256 price = _value;
        //TransferEvent = "Transfer";
        buyRole = _buyRole;
        sellRole = _sellRole;
        Location = _location;
        require(pigToOwner[_tokenId] == _from);
        ownerPigCount[_to]++;
        // 设置主人
        pigToOwner[_tokenId] = _to;
        ownerPigCount[_from]--;
        //sellBalance = sellBalance + _value;
        //buyBalance = buyBalance - _value;
        emit Transfer(_from, _to, _tokenId, _buyRole, _sellRole, _location, _value);

    }
}
