package com.comeeatme.api.comment;

import com.comeeatme.api.comment.request.CommentCreate;
import com.comeeatme.api.comment.request.CommentEdit;
import com.comeeatme.api.comment.response.CommentDto;
import com.comeeatme.api.comment.response.MemberCommentDto;
import com.comeeatme.api.common.response.CreateResult;
import com.comeeatme.api.common.response.DeleteResult;
import com.comeeatme.api.common.response.UpdateResult;
import com.comeeatme.api.exception.EntityNotFoundException;
import com.comeeatme.domain.comment.Comment;
import com.comeeatme.domain.comment.CommentEditor;
import com.comeeatme.domain.comment.repository.CommentRepository;
import com.comeeatme.domain.image.Image;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.PostImage;
import com.comeeatme.domain.post.repository.PostImageRepository;
import com.comeeatme.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    private final MemberRepository memberRepository;

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;

    @Transactional
    public CreateResult<Long> create(CommentCreate commentCreate, Long memberId, Long postId) {
        Member member = getMemberById(memberId);
        Post post = getPostWithPessimisticLockById(postId);
        Comment parent = Optional.ofNullable(commentCreate.getParentId())
                .map(this::getCommentById)
                .orElse(null);
        Comment comment = commentRepository.save(Comment.builder()
                        .member(member)
                        .post(post)
                        .parent(parent)
                        .content(commentCreate.getContent())
                .build());
        post.increaseCommentCount();
        return new CreateResult<>(comment.getId());
    }

    @Transactional
    public UpdateResult<Long> edit(CommentEdit commentEdit, Long commentId) {
        Comment comment = getCommentById(commentId);
        CommentEditor editor = comment.toEditor()
                .content(commentEdit.getContent())
                .build();
        comment.edit(editor);
        return new UpdateResult<>(comment.getId());
    }

    public boolean isNotOwnedByMember(Long commentId, Long memberId) {
        Member member = memberRepository.getReferenceById(memberId);
        return !commentRepository.existsByIdAndMember(commentId, member);
    }

    public boolean isNotBelongToPost(Long commentId, Long postId) {
        Post post = postRepository.getReferenceById(postId);
        return !commentRepository.existsByIdAndPostAndUseYnIsTrue(commentId, post);
    }

    @Transactional
    public DeleteResult<Long> delete(Long commentId) {
        Comment comment = getCommentById(commentId);
        comment.delete();
        Post post = getPostWithPessimisticLockById(comment.getPost().getId());
        post.decreaseCommentCount();
        return new DeleteResult<>(commentId);
    }

    public Slice<CommentDto> getListOfPost(Pageable pageable, Long postId) {
        Post post = getPostById(postId);
        return commentRepository.findSliceByPostWithMemberAndImage(pageable, post)
                .map(CommentDto::of);
    }

    public Slice<MemberCommentDto> getListOfMember(Pageable pageable, Long memberId) {
        Member member = getMemberById(memberId);
        Slice<Comment> comments = commentRepository.findSliceWithPostByMemberAndUseYnIsTrue(pageable, member);
        List<Post> posts = comments.stream()
                .map(Comment::getPost)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, List<Image>> postIdToImages = postImageRepository.findAllWithImageByPostIn(posts)
                .stream()
                .filter(postImage -> postImage.getImage().getUseYn())
                .collect(Collectors.groupingBy(postImage -> postImage.getPost().getId(),
                        Collectors.mapping(PostImage::getImage, Collectors.toList()))
                );
        return comments
                .map(comment -> MemberCommentDto.builder()
                        .id(comment.getId())
                        .content(comment.getContent())
                        .createdAt(comment.getCreatedAt())
                        .postId(comment.getPost().getId())
                        .postContent(comment.getPost().getContent())
                        .postImageUrl(postIdToImages.get(comment.getPost().getId()).get(0).getUrl())
                        .build()
                );
    }

    @Transactional
    public void deleteAllOfMember(Long memberId) {
        Member member = getMemberById(memberId);
        List<Comment> comments = commentRepository.findAllByMemberAndUseYnIsTrue(member);
        comments.forEach(Comment::delete);
        Map<Long, Long> postIdToCount = comments.stream()
                .map(comment -> comment.getPost().getId())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        List<Post> posts = postRepository.findAllWithPessimisticLockByIdIn(postIdToCount.keySet());
        posts.forEach(post -> {
            Long postCount = postIdToCount.get(post.getId());
            for (int i = 0; i < postCount; i++) {
                post.decreaseCommentCount();
            }
        });
    }

    private Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .filter(Member::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Member.id=" + id));
    }

    private Post getPostById(Long postId) {
        return postRepository.findById(postId)
                .filter(Post::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Post id=" + postId));
    }

    private Post getPostWithPessimisticLockById(Long postId) {
        return postRepository.findWithPessimisticLockById(postId)
                .filter(Post::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Post id=" + postId));
    }

    private Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .filter(Comment::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Comment id=" + commentId));
    }
}
