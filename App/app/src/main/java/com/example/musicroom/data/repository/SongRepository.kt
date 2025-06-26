package com.example.musicroom.data.repository

import com.example.musicroom.data.models.Song

class SongRepository {
    
    suspend fun getRecommendedSongs(): List<Song> {
        // Simulate network delay
        kotlinx.coroutines.delay(1000)
        
        return listOf(
            Song(
                id = "421702",
                name = "Ear3 - Believe your ears",
                duration = 503,
                artistId = "353333",
                artistName = "Ear3",
                albumName = "Believe your Ears",
                albumId = "51694",
                releasedate = "2009-09-08",
                image = "https://usercontent.jamendo.com?type=album&id=51694&width=300&trackid=421702",
                audio = "https://prod-1.storage.jamendo.com/?trackid=421702&format=mp31&from=eMJy17etfk%2Bp4EMDuvY10g%3D%3D%7Cc1hz%2FhTtEepUgdWZpKynzg%3D%3D",
                audiodownload = "https://prod-1.storage.jamendo.com/download/track/421702/mp32/",
                shorturl = "https://jamen.do/t/421702",
                shareurl = "https://www.jamendo.com/track/421702",
                audiodownloadAllowed = true
            ),
            Song(
                id = "113592",
                name = "Lumina",
                duration = 272,
                artistId = "337467",
                artistName = "Skala",
                albumName = "Lumina",
                albumId = "14826",
                releasedate = "2007-12-03",
                image = "https://usercontent.jamendo.com?type=album&id=14826&width=300&trackid=113592",
                audio = "https://prod-1.storage.jamendo.com/?trackid=113592&format=mp31&from=RpAx1sRru9aMYrIQQaKNVw%3D%3D%7CoM3gdXPFMsfbZmW%2B75m06g%3D%3D",
                audiodownload = "https://prod-1.storage.jamendo.com/download/track/113592/mp32/",
                shorturl = "https://jamen.do/t/113592",
                shareurl = "https://www.jamendo.com/track/113592",
                audiodownloadAllowed = true
            ),
            Song(
                id = "2200958",
                name = "Amy Rocks",
                duration = 186,
                artistId = "595223",
                artistName = "Concrete Garden",
                albumName = "Amy Rocks",
                albumId = "578959",
                releasedate = "2024-09-05",
                image = "https://usercontent.jamendo.com?type=album&id=578959&width=300&trackid=2200958",
                audio = "https://prod-1.storage.jamendo.com/?trackid=2200958&format=mp31&from=NNmmBC23c465hJaykNe%2FDw%3D%3D%7CFDRYypJVtfCCi%2BKLb4%2B2lw%3D%3D",
                audiodownload = "https://prod-1.storage.jamendo.com/download/track/2200958/mp32/",
                shorturl = "https://jamen.do/t/2200958",
                shareurl = "https://www.jamendo.com/track/2200958",
                audiodownloadAllowed = true
            ),
            Song(
                id = "1098161",
                name = "Cube",
                duration = 216,
                artistId = "439286",
                artistName = "PKRZ",
                albumName = "The Others",
                albumId = "130984",
                releasedate = "2014-01-26",
                image = "https://usercontent.jamendo.com?type=album&id=130984&width=300&trackid=1098161",
                audio = "https://prod-1.storage.jamendo.com/?trackid=1098161&format=mp31&from=%2B4rLQBKiBQCx7BwKch7W1w%3D%3D%7CmXsTwOSGNHlSCD2ng1Wiwg%3D%3D",
                audiodownload = "",
                shorturl = "https://jamen.do/t/1098161",
                shareurl = "https://www.jamendo.com/track/1098161",
                audiodownloadAllowed = false
            ),
            Song(
                id = "1952007",
                name = "Sunrise",
                duration = 163,
                artistId = "547538",
                artistName = "Wally",
                albumName = "Different emotions",
                albumId = "486010",
                releasedate = "2022-06-13",
                image = "https://usercontent.jamendo.com?type=album&id=486010&width=300&trackid=1952007",
                audio = "https://prod-1.storage.jamendo.com/?trackid=1952007&format=mp31&from=Re3N%2FABQzFwpMAZqzZAl6A%3D%3D%7CXTLMhGywC6s%2BilahUgb%2FCQ%3D%3D",
                audiodownload = "",
                shorturl = "https://jamen.do/t/1952007",
                shareurl = "https://www.jamendo.com/track/1952007",
                audiodownloadAllowed = false
            )
        )
    }
    
    suspend fun getPopularSongs(): List<Song> {
        // Simulate network delay
        kotlinx.coroutines.delay(1000)
        
        return listOf(
            Song(
                id = "168",
                name = "J'm'e FPM",
                duration = 183,
                artistId = "7",
                artistName = "TriFace",
                albumName = "Premiers Jets",
                albumId = "24",
                releasedate = "2004-12-17",
                image = "https://usercontent.jamendo.com?type=album&id=24&width=300&trackid=168",
                audio = "https://prod-1.storage.jamendo.com/?trackid=168&format=mp31&from=THbQxJL8ifCsjMcrJP6yaw%3D%3D%7CbIeGF6RI2K8VVTFW0dSLOQ%3D%3D",
                audiodownload = "https://prod-1.storage.jamendo.com/download/track/168/mp32/",
                shorturl = "https://jamen.do/t/168",
                shareurl = "https://www.jamendo.com/track/168",
                audiodownloadAllowed = true
            ),
            Song(
                id = "169",
                name = "Trio HxC",
                duration = 101,
                artistId = "7",
                artistName = "TriFace",
                albumName = "Premiers Jets",
                albumId = "24",
                releasedate = "2004-12-17",
                image = "https://usercontent.jamendo.com?type=album&id=24&width=300&trackid=169",
                audio = "https://prod-1.storage.jamendo.com/?trackid=169&format=mp31&from=%2FUFiU793ZAkTl33klGo9xg%3D%3D%7Clup5Qkf8qcogoVobG0PUOw%3D%3D",
                audiodownload = "https://prod-1.storage.jamendo.com/download/track/169/mp32/",
                shorturl = "https://jamen.do/t/169",
                shareurl = "https://www.jamendo.com/track/169",
                audiodownloadAllowed = true
            ),
            Song(
                id = "170",
                name = "Un Poil De Relifion",
                duration = 207,
                artistId = "7",
                artistName = "TriFace",
                albumName = "Premiers Jets",
                albumId = "24",
                releasedate = "2004-12-17",
                image = "https://usercontent.jamendo.com?type=album&id=24&width=300&trackid=170",
                audio = "https://prod-1.storage.jamendo.com/?trackid=170&format=mp31&from=5bDyS7nsyHQUkQXrgSRBVA%3D%3D%7CHNVAWEG%2BFoXKlZ9dmHvbVg%3D%3D",
                audiodownload = "https://prod-1.storage.jamendo.com/download/track/170/mp32/",
                shorturl = "https://jamen.do/t/170",
                shareurl = "https://www.jamendo.com/track/170",
                audiodownloadAllowed = true
            ),
            Song(
                id = "171",
                name = "Apologies",
                duration = 145,
                artistId = "7",
                artistName = "TriFace",
                albumName = "Premiers Jets",
                albumId = "24",
                releasedate = "2004-12-17",
                image = "https://usercontent.jamendo.com?type=album&id=24&width=300&trackid=171",
                audio = "https://prod-1.storage.jamendo.com/?trackid=171&format=mp31&from=nBr0K0er5FJKTta7yB47jQ%3D%3D%7CU%2FIKPd19WOFpavY9mpEyGg%3D%3D",
                audiodownload = "https://prod-1.storage.jamendo.com/download/track/171/mp32/",
                shorturl = "https://jamen.do/t/171",
                shareurl = "https://www.jamendo.com/track/171",
                audiodownloadAllowed = true
            ),
            Song(
                id = "172",
                name = "Je Vomis Comme Je Chante",
                duration = 177,
                artistId = "7",
                artistName = "TriFace",
                albumName = "Premiers Jets",
                albumId = "24",
                releasedate = "2004-12-17",
                image = "https://usercontent.jamendo.com?type=album&id=24&width=300&trackid=172",
                audio = "https://prod-1.storage.jamendo.com/?trackid=172&format=mp31&from=aqCTnXkW%2Bts4tw5icoAMAA%3D%3D%7CmxA6SEr5VeFW7XJr%2FxgUbA%3D%3D",
                audiodownload = "https://prod-1.storage.jamendo.com/download/track/172/mp32/",
                shorturl = "https://jamen.do/t/172",
                shareurl = "https://www.jamendo.com/track/172",
                audiodownloadAllowed = true
            )
        )
    }
}