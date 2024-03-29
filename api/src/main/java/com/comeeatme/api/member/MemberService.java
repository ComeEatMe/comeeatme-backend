package com.comeeatme.api.member;

import com.comeeatme.api.common.response.CreateResult;
import com.comeeatme.api.common.response.DeleteResult;
import com.comeeatme.api.common.response.DuplicateResult;
import com.comeeatme.api.common.response.UpdateResult;
import com.comeeatme.api.exception.AlreadyNicknameExistsException;
import com.comeeatme.api.exception.EntityNotFoundException;
import com.comeeatme.api.member.request.MemberEdit;
import com.comeeatme.api.member.request.MemberSearch;
import com.comeeatme.api.member.response.MemberDetailDto;
import com.comeeatme.api.member.response.MemberSimpleDto;
import com.comeeatme.domain.image.Image;
import com.comeeatme.domain.image.repository.ImageRepository;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.MemberDeleteReason;
import com.comeeatme.domain.member.MemberEditor;
import com.comeeatme.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    private final ImageRepository imageRepository;

    @Transactional
    public UpdateResult<Long> edit(MemberEdit memberEdit, Long memberId) {
        Member member = getMemberById(memberId);
        MemberEditor editor = member.toEditor()
                .nickname(memberEdit.getNickname())
                .introduction(memberEdit.getIntroduction())
                .build();
        member.edit(editor);
        return new UpdateResult<>(member.getId());
    }

    @Transactional
    public UpdateResult<Long> editImage(Long memberId, Long imageId) {
        Member member = getMemberById(memberId);
        Optional.ofNullable(member.getImage())
                .ifPresent(Image::delete);

        Image image = getImageById(imageId);
        MemberEditor editor = member.toEditor()
                .image(image)
                .build();
        member.edit(editor);

        return new UpdateResult<>(member.getId());
    }

    @Transactional
    public DeleteResult<Long> deleteImage(Long memberId) {
        Member member = getMemberById(memberId);
        Optional.ofNullable(member.getImage())
                .ifPresent(Image::delete);
        MemberEditor editor = member.toEditor()
                .image(null)
                .build();
        member.edit(editor);

        return new DeleteResult<>(member.getId());
    }

    public Slice<MemberSimpleDto> search(Pageable pageable, MemberSearch memberSearch) {
        return memberRepository.findSliceWithImagesByNicknameStartingWith(pageable, memberSearch.getNickname())
                .map(MemberSimpleDto::of);
    }

    public MemberDetailDto get(Long id) {
        Member member = getMemberById(id);
        return MemberDetailDto.of(member);
    }

    public DuplicateResult checkNicknameDuplicate(String nickname) {
        return DuplicateResult.builder()
                .duplicate(memberRepository.existsByNickname(nickname))
                .build();
    }

    @Transactional
    public CreateResult<Long> create(String nickname) {
        if (memberRepository.existsByNickname(nickname)) {
            throw new AlreadyNicknameExistsException("nickname=" + nickname);
        }
        Member member = memberRepository.save(Member.builder()
                .nickname(nickname)
                .introduction("")
                .build());
        return new CreateResult<>(member.getId());
    }

    @Transactional
    public DeleteResult<Long> delete(Long memberId) {
        Member member = getMemberById(memberId);
        member.delete();
        return new DeleteResult<>(member.getId());
    }

    @Transactional
    public void registerDeleteReason(Long memberId, MemberDeleteReason reason) {
        Member member = getMemberById(memberId);
        member.setDeleteReason(reason);
    }

    private Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .filter(Member::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Member id=" + id));
    }

    private Image getImageById(Long imageId) {
        return imageRepository.findById(imageId)
                .filter(Image::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Images id=" + imageId));
    }
}
