package com.ixnah.hmcl.pf4j;

import com.github.zafarkhaja.semver.Version;
import org.pf4j.VersionManager;
import org.pf4j.util.StringUtils;

public class HmclVersionManager implements VersionManager {
    @Override
    public boolean checkVersionConstraint(String version, String constraint) {
        return StringUtils.isNullOrEmpty(constraint) || "*".equals(constraint) || parse(version).satisfies(constraint);
    }

    @Override
    public int compareVersions(String v1, String v2) {
        return parse(v1).compareTo(parse(v2));
    }

    public static Version parse(String version) {
        StringBuilder stringBuilder = new StringBuilder();
        Version result = null;
        int dotCount = 0;
        for (char c : version.toCharArray()) {
            if ('.' == c) dotCount ++;
            if (dotCount > 2 && result == null) {
                result = Version.parse(stringBuilder.toString(), false);
                stringBuilder.setLength(0);
                continue;
            }
            stringBuilder.append(c);
        }
        if (result == null) {
            result = Version.parse(version, false);
        } else if (stringBuilder.length() > 0) {
            result = result.withBuildMetadata(stringBuilder.toString());
        }
        return result;
    }
}
