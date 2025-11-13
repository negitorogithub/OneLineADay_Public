package net.unifar.mydiary.util

import net.unifar.mydiary.data.local.entity.Diary
import java.text.SimpleDateFormat
import java.util.Locale

val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN)
val demoDiaries = listOf(
    Diary(
        "demo_1",
        "今日は一日中雨だった。家で本を読んで過ごした。",
        dateFormat.parse("2025-05-19")!!
    ),
    Diary(
        "demo_2",
        "久々に友達とランチ。",
        dateFormat.parse("2025-05-18")!!
    ),
    Diary(
        "demo_3",
        "朝からジョギング。天気がよくて気持ちよかった。",
        dateFormat.parse("2025-05-17")!!
    ),
    Diary(
        "demo_4",
        "勉強がはかどった日。集中力が続くと嬉しい。",
        dateFormat.parse("2025-05-16")!!
    ),
    Diary(
        "demo_5",
        "電車で席を譲ってもらった。なんだか心が温まった。",
        dateFormat.parse("2025-05-15")!!
    ),
    Diary(
        "demo_6",
        "疲れたので早めに寝た。睡眠、大事。",
        dateFormat.parse("2025-05-14")!!
    ),
    Diary(
        "demo_7",
        "映画『名前のない日々』を観た。静かで、深い作品だった。",
        dateFormat.parse("2025-05-13")!!
    ),
    Diary(
        "demo_8",
        "コーヒーがいつもより美味しく感じた。",
        dateFormat.parse("2025-05-12")!!
    ),
    Diary(
        "demo_9",
        "仕事でちょっとしたミス。次は気をつけよう。",
        dateFormat.parse("2025-05-11")!!
    ),
    Diary(
        "demo_10",
        "空が綺麗で写真を撮った。初夏の空っていいな。",
        dateFormat.parse("2025-05-10")!!
    )
)

val demoDiaries_en = listOf(
    Diary(
        "demo_1_en",
        "It rained all day today. I stayed home and read a book.",
        dateFormat.parse("2025-05-19")!!
    ),
    Diary(
        "demo_2_en",
        "Had lunch with a friend for the first time in a while.",
        dateFormat.parse("2025-05-18")!!
    ),
    Diary(
        "demo_3_en",
        "Went jogging in the morning. The weather was nice and refreshing.",
        dateFormat.parse("2025-05-17")!!
    ),
    Diary(
        "demo_4_en",
        "Had a productive study day. Feels good to stay focused.",
        dateFormat.parse("2025-05-16")!!
    ),
    Diary(
        "demo_5_en",
        "Someone gave up their seat for me on the train. It warmed my heart.",
        dateFormat.parse("2025-05-15")!!
    ),
    Diary(
        "demo_6_en",
        "Went to bed early because I was tired. Sleep really matters.",
        dateFormat.parse("2025-05-14")!!
    ),
    Diary(
        "demo_7_en",
        "Watched the movie \"Days Without a Name\". It was quiet and profound.",
        dateFormat.parse("2025-05-13")!!
    ),
    Diary(
        "demo_8_en",
        "My coffee tasted better than usual today.",
        dateFormat.parse("2025-05-12")!!
    ),
    Diary(
        "demo_9_en",
        "Made a small mistake at work. I'll be more careful next time.",
        dateFormat.parse("2025-05-11")!!
    ),
    Diary(
        "demo_10_en",
        "The sky was beautiful, so I took a picture. Early summer skies are lovely.",
        dateFormat.parse("2025-05-10")!!
    )
)
