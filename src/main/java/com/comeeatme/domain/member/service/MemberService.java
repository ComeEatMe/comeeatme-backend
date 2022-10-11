package com.comeeatme.domain.member.service;

import com.comeeatme.domain.common.response.DuplicateResult;
import com.comeeatme.domain.images.Images;
import com.comeeatme.domain.images.repository.ImagesRepository;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.MemberEditor;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.member.request.MemberEdit;
import com.comeeatme.domain.member.request.MemberSearch;
import com.comeeatme.domain.member.response.MemberSimpleDto;
import com.comeeatme.error.exception.EntityAccessDeniedException;
import com.comeeatme.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.nonNull;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    private final ImagesRepository imagesRepository;

    @Transactional
    public Long edit(MemberEdit memberEdit, String username) {
        Member member = getMemberByUsername(username);
        MemberEditor.MemberEditorBuilder editorBuilder = member.toEditor()
                .nickname(memberEdit.getNickname())
                .introduction(memberEdit.getIntroduction());
        editMemberImage(memberEdit, member, editorBuilder);
        MemberEditor editor = editorBuilder.build();
        member.edit(editor);
        return member.getId();
    }

    public Slice<MemberSimpleDto> search(Pageable pageable, MemberSearch memberSearch) {
        return memberRepository.findSliceWithImagesByNicknameStartingWith(pageable, memberSearch.getNickname())
                .map(MemberSimpleDto::of);
    }

    private void editMemberImage(MemberEdit memberEdit, Member member, MemberEditor.MemberEditorBuilder editorBuilder) {
        Long memberImageId = Optional.ofNullable(member.getImage())
                .filter(Images::getUseYn)
                .map(Images::getId)
                .orElse(null);
        if (!Objects.equals(memberImageId, memberEdit.getImageId())) {
            if (nonNull(memberImageId)) {
                member.getImage().delete();
            }
            if (nonNull(memberEdit.getImageId())) {
                Images image = getImageById(memberEdit.getImageId());
                validateImageOwner(member, image);
                editorBuilder.image(image);
            }
        }
    }

    private void validateImageOwner(Member member, Images image) {
        if (!Objects.equals(image.getMember().getId(), member.getId())) {
            throw new EntityAccessDeniedException(String.format(
                    "image.member.id=%s, member.id=%s", image.getMember().getId(), member.getId()));
        }
    }

    private Member getMemberByUsername(String username) {
        return memberRepository.findByUsername(username)
                .filter(Member::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Member username=" + username));
    }

    private Images getImageById(Long imageId) {
        return imagesRepository.findById(imageId)
                .filter(Images::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Images id=" + imageId));
    }

    public DuplicateResult checkNicknameDuplicate(String nickname) {
        return DuplicateResult.builder()
                .duplicate(memberRepository.existsByNickname(nickname))
                .build();
    }
}
