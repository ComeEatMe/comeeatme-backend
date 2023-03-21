package com.comeeatme.api.member;

import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.UUID;

@Component
public class MemberNicknameCreator {

    private static final String[] ADJECTIVES = {
            "달달한", "달콤한", "짭짤한", "싱거운", "기름진", "쫄깃한", "시큼한", "매운", "고소한", "바삭한", "딱딱한",
            "촉촉한", "쌉쌀한", "떫은", "질긴", "상큼한", "새콤한", "아삭한", "눅눅한", "밍밍한", "칼칼한", "얼큰한",
            "구수한", "느끼한", "알싸한", "비린", "구수한", "꼬릿한", "담백한"
    };

    private static final String[] NOUNS = {
            "짜장면", "짬뽕", "삼겹살", "제육볶음", "된장찌개", "돈까스", "탕수육", "치킨", "양념치킨", "떡볶이",
            "부추전", "김치전", "감자탕", "먹태", "쥐포", "초밥", "우동", "라면", "순대", "수육국밥", "해장국",
            "쌀국수", "청국장", "김치찌개", "파스타", "냉면", "불고기", "김치", "단무지", "깍두기", "햄버거", "피자",
            "족발", "보쌈", "곱창", "샐러드", "해물찜", "낙지볶음", "비빔밥", "조개찜", "추어탕", "매운탕", "알탕",
            "삼계탕", "갈비탕", "갈비찜", "토스트", "고로케", "닭강정", "낙곱새", "닭볶음탕", "찜닭", "부대찌개"
    };

    private final Random random = new Random();

    public String create() {
        String adjective = ADJECTIVES[random.nextInt(ADJECTIVES.length)];
        String noun = NOUNS[random.nextInt(NOUNS.length)];
        String postfix = UUID.randomUUID().toString().substring(0, 6);
        return adjective + noun + postfix;
    }
}
