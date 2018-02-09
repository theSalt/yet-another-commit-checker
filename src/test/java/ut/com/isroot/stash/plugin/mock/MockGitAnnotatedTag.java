package ut.com.isroot.stash.plugin.mock;

import java.util.Optional;

import com.atlassian.bitbucket.repository.RefType;
import com.atlassian.bitbucket.repository.StandardRefType;
import com.atlassian.bitbucket.scm.git.ref.GitAnnotatedTag;
import com.atlassian.bitbucket.user.Person;

public class MockGitAnnotatedTag implements GitAnnotatedTag {

	private String hash;
	private String latestCommit;
	private String displayId;
	private String id;
	private RefType refType;
	private Person tagger;
	
	public MockGitAnnotatedTag(String taggerName, String taggerEmailAddress) {
		this.hash = "69b0f8590e325c7aafe7cb88ce4be8b7916a1e8a";
		this.latestCommit = "87897afe082b782d08dd8d9264df697cb93ff78a";
		this.displayId = "yacc-1.18";
		this.id = "32ef96f";
		this.refType = StandardRefType.TAG;
		this.tagger = new Person() {
			
			@Override
			public String getName() {
				return taggerName;
			}
			
			@Override
			public String getEmailAddress() {
				return taggerEmailAddress;
			}
		};
	}
	
	@Override
	public String getHash() {
		return hash;
	}

	@Override
	public String getLatestCommit() {
		return latestCommit;
	}

	@Override
	public String getDisplayId() {
		return displayId;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public RefType getType() {
		return refType;
	}

	@Override
	public Optional<String> getMessage() {
		return Optional.empty();
	}

	@Override
	public Person getTagger() {
		return tagger;
	}

}
