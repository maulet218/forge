package forge.util;

import forge.ImageKeys;
import forge.StaticData;
import forge.card.CardDb;
import forge.card.CardRules;
import forge.card.CardSplitType;
import forge.item.PaperCard;

public class ImageUtil {
    public static float getNearestHQSize(float baseSize, float actualSize) {
        //get nearest power of actualSize to baseSize so that the image renders good
        return (float)Math.round(actualSize) * (float)Math.pow(2, (double)Math.round(Math.log(baseSize / actualSize) / Math.log(2)));
    }

    public static PaperCard getPaperCardFromImageKey(String key) {
        if (key == null || key.length() < 2) {
            return null;
        }

        key = key.substring(2);
        PaperCard cp = StaticData.instance().getCommonCards().getCard(key);
        if (cp == null) {
            cp = StaticData.instance().getVariantCards().getCard(key);
        }
        return cp;
    }

    public static String getImageRelativePath(PaperCard cp, String face, boolean includeSet, boolean isDownloadUrl) {
        final String nameToUse = cp == null ? null : getNameToUse(cp, face);
        if (nameToUse == null) {
            return null;
        }
        StringBuilder s = new StringBuilder();

        CardRules card = cp.getRules();
        String edition = cp.getEdition();
        s.append(toMWSFilename(nameToUse));

        final int cntPictures;
        final boolean hasManyPictures;
        final CardDb db =  !card.isVariant() ? StaticData.instance().getCommonCards() : StaticData.instance().getVariantCards();
        if (includeSet) {
            cntPictures = db.getArtCount(card.getName(), edition);
            hasManyPictures = cntPictures > 1;
        } else {
            cntPictures = 1;
            // raise the art index limit to the maximum of the sets this card was printed in
            int maxCntPictures = db.getMaxArtIndex(card.getName());
            hasManyPictures = maxCntPictures > 1;
        }

        int artIdx = cp.getArtIndex() - 1;
        if (hasManyPictures) {
            if (cntPictures <= artIdx) // prevent overflow
                artIdx = artIdx % cntPictures;
            s.append(artIdx + 1);
        }

        // for whatever reason, MWS-named plane cards don't have the ".full" infix
        if (!card.getType().isPlane() && !card.getType().isPhenomenon()) {
            s.append(".full");
        }

        String fname;
        if (isDownloadUrl) {
            s.append(".jpg");
            fname = s.toString().replaceAll("\\s", "%20");
        } else {
            fname = s.toString();
        }

        if (includeSet) {
            String editionAliased = isDownloadUrl ? StaticData.instance().getEditions().getCode2ByCode(edition) : ImageKeys.getSetFolder(edition);
            if (editionAliased == "") //FIXME: Custom Cards Workaround
                editionAliased = edition;
            return TextUtil.concatNoSpace(editionAliased, "/", fname);
        } else {
            return fname;
        }
    }

    public static String getNameToUse(PaperCard cp, String face) {
        final CardRules card = cp.getRules();
        if (face.equals("back")) {
            if (cp.hasBackFace())
                if (card.getOtherPart() != null) {
                    return card.getOtherPart().getName();
                } else if (!card.getMeldWith().isEmpty()) {
                    final CardDb db = StaticData.instance().getCommonCards();
                    return db.getRules(card.getMeldWith()).getOtherPart().getName();
                } else {
                    return null;
                }
            else
                return null;
        } else if (face.equals("white")) {
            if (card.getWSpecialize() != null) {
                return card.getWSpecialize().getName();
            }
        } else if (face.equals("blue")) {
            if (card.getUSpecialize() != null) {
                return card.getUSpecialize().getName();
            }
        } else if (face.equals("black")) {
            if (card.getBSpecialize() != null) {
                return card.getBSpecialize().getName();
            }
        } else if (face.equals("red")) {
            if (card.getRSpecialize() != null) {
                return card.getRSpecialize().getName();
            }
        } else if (face.equals("green")) {
            if (card.getGSpecialize() != null) {
                return card.getGSpecialize().getName();
            }
        } else if (CardSplitType.Split == cp.getRules().getSplitType()) {
            return card.getMainPart().getName() + card.getOtherPart().getName();
        }
        return cp.getName();
    }

    public static String getImageKey(PaperCard cp, String face, boolean includeSet) {
        return getImageRelativePath(cp, face, includeSet, false);
    }

    public static String getDownloadUrl(PaperCard cp, String face) {
        return getImageRelativePath(cp, face, true, true);
    }

    public static String getScryfallDownloadUrl(PaperCard cp, String face, String setCode, String langCode, boolean useArtCrop){
        String editionCode;
        if ((setCode != null) && (setCode.length() > 0))
            editionCode = setCode;
        else
            editionCode = cp.getEdition().toLowerCase();
        String cardCollectorNumber = cp.getCollectorNumber();
        // Hack to account for variations in Arabian Nights
        cardCollectorNumber = cardCollectorNumber.replace("+", "†");
        String versionParam = useArtCrop ? "art_crop" : "normal";
        String faceParam = "";
        if (cp.getRules().getOtherPart() != null) {
            faceParam = (face.equals("back") ? "&face=back" : "&face=front");
        }
        return String.format("%s/%s/%s?format=image&version=%s%s", editionCode, cardCollectorNumber,
                langCode, versionParam, faceParam);
    }

    public static String toMWSFilename(String in) {
        final StringBuilder out = new StringBuilder();
        char c;
        for (int i = 0; i < in.length(); i++) {
            c = in.charAt(i);
            if ((c == '"') || (c == '/') || (c == ':') || (c == '?')) {
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}